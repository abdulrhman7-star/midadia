package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.SavedDocument
import com.example.data.model.SupportedLanguage
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MidaddRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireMatchingUid(userId: String): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        check(uid == userId && uid.isNotBlank()) { "Authenticated user mismatch." }
        return uid
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) null
                else snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    fun observeSavedDocuments(userId: String): Flow<List<SavedDocument>> {
        val colRef = db.collection("users").document(userId).collection("documents")
        return colRef
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SavedDocument::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, colRef.path)
                }
                throw error
            }
    }

    suspend fun ensureUserProfile(
        userId: String,
        displayName: String,
        email: String,
        preferredLanguage: SupportedLanguage,
        darkMode: Boolean
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireMatchingUid(userId)
            val ref = db.collection("users").document(uid)
            val snap = ref.get().await()
            if (!snap.exists()) {
                val safeName = displayName.trim().ifBlank { "User" }.take(80)
                val safeEmail = email.trim().take(160)
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "credits" to 25L,
                    "maxCredits" to 25L,
                    "preferredLanguage" to preferredLanguage.code,
                    "darkMode" to darkMode,
                    "favoriteTemplateIds" to emptyList<String>(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                ref.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateDisplayName(userId: String, newName: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireMatchingUid(userId)
            val safeName = newName.trim().take(80)
            require(safeName.isNotEmpty()) { "Display name cannot be empty" }
            db.collection("users").document(uid).update(
                mapOf(
                    "displayName" to safeName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updatePreferences(userId: String, language: SupportedLanguage, darkMode: Boolean): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireMatchingUid(userId)
            db.collection("users").document(uid).update(
                mapOf(
                    "preferredLanguage" to language.code,
                    "darkMode" to darkMode,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleFavoriteTemplate(
        userId: String,
        templateId: String,
        currentFavorites: List<String>
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireMatchingUid(userId)
            val updated = if (templateId in currentFavorites) {
                currentFavorites.filterNot { it == templateId }
            } else {
                (currentFavorites + templateId).take(50)
            }
            db.collection("users").document(uid).update(
                mapOf(
                    "favoriteTemplateIds" to updated,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deductCredit(userId: String, currentCredits: Long): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireMatchingUid(userId)
            val nextCredits = (currentCredits - 1L).coerceAtLeast(0L)
            db.collection("users").document(uid).update(
                mapOf(
                    "credits" to nextCredits,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun addCredits(userId: String, currentCredits: Long, currentMax: Long, amount: Long): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireMatchingUid(userId)
            val nextCredits = (currentCredits + amount).coerceIn(0L, 100000L)
            val nextMax = maxOf(currentMax, nextCredits).coerceIn(1L, 100000L)
            db.collection("users").document(uid).update(
                mapOf(
                    "credits" to nextCredits,
                    "maxCredits" to nextMax,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveDocument(
        userId: String,
        docId: String?,
        title: String,
        markdown: String,
        direction: String,
        orientation: String,
        margins: String,
        fontFamily: String,
        fontSizeSp: Long,
        watermark: String
    ): Result<String> {
        val safeTitle = title.trim().ifBlank { "Untitled Document" }.take(200)
        val safeMarkdown = markdown.take(150000)
        val safeDirection = if (direction == "rtl") "rtl" else "ltr"
        val safeOrientation = if (orientation == "landscape") "landscape" else "portrait"
        val safeMargins = if (margins in setOf("normal", "narrow", "wide")) margins else "normal"
        val safeFont = fontFamily.trim().ifBlank { "Cairo" }.take(60)
        val safeSize = fontSizeSp.coerceIn(8L, 32L)
        val safeWatermark = watermark.trim().take(80)

        var path = "users/$userId/documents"
        return try {
            val uid = requireMatchingUid(userId)
            val colRef = db.collection("users").document(uid).collection("documents")
            val isUpdate = !docId.isNullOrBlank()
            val docRef = if (isUpdate) colRef.document(docId!!) else colRef.document()
            path = docRef.path

            if (isUpdate) {
                docRef.update(
                    mapOf(
                        "title" to safeTitle,
                        "markdown" to safeMarkdown,
                        "direction" to safeDirection,
                        "orientation" to safeOrientation,
                        "margins" to safeMargins,
                        "fontFamily" to safeFont,
                        "fontSizeSp" to safeSize,
                        "watermark" to safeWatermark,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "title" to safeTitle,
                        "markdown" to safeMarkdown,
                        "direction" to safeDirection,
                        "orientation" to safeOrientation,
                        "margins" to safeMargins,
                        "fontFamily" to safeFont,
                        "fontSizeSp" to safeSize,
                        "watermark" to safeWatermark,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (docId.isNullOrBlank()) OperationType.CREATE else OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(userId: String, docId: String): Result<Unit> {
        val path = "users/$userId/documents/$docId"
        return try {
            val uid = requireMatchingUid(userId)
            require(docId.isNotBlank()) { "Document ID cannot be blank" }
            db.collection("users").document(uid).collection("documents").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
