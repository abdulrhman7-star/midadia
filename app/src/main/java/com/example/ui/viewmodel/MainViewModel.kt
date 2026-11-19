package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.i18n.AppStringsProvider
import com.example.data.model.DocumentStats
import com.example.data.model.EditorViewMode
import com.example.data.model.GuideArticle
import com.example.data.model.PageMargins
import com.example.data.model.PageOrientation
import com.example.data.model.RibbonTab
import com.example.data.model.SavedDocument
import com.example.data.model.SupportedLanguage
import com.example.data.model.TemplateItem
import com.example.data.model.TextDirectionMode
import com.example.data.model.UserProfile
import com.example.data.repository.MidaddRepository
import com.example.data.repository.UserPreferencesStore
import com.example.domain.markdown.DocxExportOptions
import com.example.domain.markdown.DocxExporter
import com.example.domain.markdown.DocumentStatsCalculator
import com.example.domain.markdown.ImportedDocxResult
import com.example.domain.markdown.MarkdownToHtml
import com.example.domain.markdown.ShareLinkFetcher
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class EditorUiState(
    val currentDocId: String? = null,
    val title: String = "Untitled Document",
    val markdown: String = MarkdownToHtml.SAMPLE_MARKDOWN_PRESETS.first().markdown,
    val direction: TextDirectionMode = TextDirectionMode.RTL,
    val orientation: PageOrientation = PageOrientation.PORTRAIT,
    val margins: PageMargins = PageMargins.NORMAL,
    val fontFamily: String = "Cairo",
    val fontSizeSp: Int = 14,
    val watermark: String = "",
    val zoomPercent: Int = 100,
    val showRuler: Boolean = true,
    val viewMode: EditorViewMode = EditorViewMode.SPLIT,
    val activeRibbonTab: RibbonTab = RibbonTab.HOME,
    val undoStack: List<String> = emptyList(),
    val redoStack: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isFetchingShareLink: Boolean = false,
    val isExporting: Boolean = false,
    val showOutOfCreditsDialog: Boolean = false
)

class MainViewModel(
    private val repository: MidaddRepository,
    private val prefsStore: UserPreferencesStore,
    val templates: List<TemplateItem>,
    val guides: List<GuideArticle>,
    val currentUserId: String,
    private val authEmail: String,
    private val authDisplayName: String
) : ViewModel() {

    val language: StateFlow<SupportedLanguage> = prefsStore.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SupportedLanguage.AR)

    val darkMode: StateFlow<Boolean> = prefsStore.darkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val userProfileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing profile", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load user profile"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val savedDocumentsState: StateFlow<UiState<List<SavedDocument>>> = repository.observeSavedDocuments(currentUserId)
        .map<List<SavedDocument>, UiState<List<SavedDocument>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing documents", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load saved documents"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val _editorState = MutableStateFlow(EditorUiState())
    val editorState: StateFlow<EditorUiState> = _editorState.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            val lang = prefsStore.languageFlow.first()
            val dark = prefsStore.darkModeFlow.first()
            repository.ensureUserProfile(
                userId = currentUserId,
                displayName = authDisplayName.ifBlank { "User" },
                email = authEmail,
                preferredLanguage = lang,
                darkMode = dark
            )
            if (lang != SupportedLanguage.AR) {
                _editorState.update {
                    it.copy(
                        direction = TextDirectionMode.LTR,
                        fontFamily = "Plus Jakarta Sans"
                    )
                }
            }
        }
    }

    fun calculateStats(): DocumentStats = DocumentStatsCalculator.calculate(_editorState.value.markdown)

    fun updateTitle(title: String) {
        _editorState.update { it.copy(title = title.take(200)) }
    }

    fun updateMarkdown(newMarkdown: String, recordHistory: Boolean = true) {
        _editorState.update { state ->
            if (newMarkdown == state.markdown) return@update state
            val nextUndo = if (recordHistory) {
                (state.undoStack + state.markdown).takeLast(25)
            } else {
                state.undoStack
            }
            state.copy(
                markdown = newMarkdown,
                undoStack = nextUndo,
                redoStack = if (recordHistory) emptyList() else state.redoStack
            )
        }
    }

    fun undo() {
        _editorState.update { state ->
            val prev = state.undoStack.lastOrNull() ?: return@update state
            state.copy(
                markdown = prev,
                undoStack = state.undoStack.dropLast(1),
                redoStack = (state.redoStack + state.markdown).takeLast(25)
            )
        }
    }

    fun redo() {
        _editorState.update { state ->
            val next = state.redoStack.lastOrNull() ?: return@update state
            state.copy(
                markdown = next,
                redoStack = state.redoStack.dropLast(1),
                undoStack = (state.undoStack + state.markdown).takeLast(25)
            )
        }
    }

    fun insertSnippet(snippet: String) {
        val current = _editorState.value.markdown
        val separator = if (current.isBlank() || current.endsWith("\n")) "" else "\n\n"
        updateMarkdown(current + separator + snippet, recordHistory = true)
    }

    fun wrapOrAppendFormat(prefix: String, suffix: String, placeholder: String) {
        val current = _editorState.value.markdown
        val block = "$prefix$placeholder$suffix"
        val sep = if (current.isBlank() || current.endsWith("\n") || current.endsWith(" ")) "" else " "
        updateMarkdown(current + sep + block, recordHistory = true)
    }

    fun replaceDocumentContent(newTitle: String, newMarkdown: String, newDirection: TextDirectionMode? = null) {
        _editorState.update { state ->
            state.copy(
                currentDocId = null,
                title = newTitle.ifBlank { "Untitled Document" }.take(200),
                markdown = newMarkdown,
                direction = newDirection ?: state.direction,
                undoStack = (state.undoStack + state.markdown).takeLast(25),
                redoStack = emptyList()
            )
        }
    }

    fun clearDocument() {
        _editorState.update { state ->
            state.copy(
                currentDocId = null,
                title = "Untitled Document",
                markdown = "",
                watermark = "",
                undoStack = (state.undoStack + state.markdown).takeLast(25),
                redoStack = emptyList()
            )
        }
    }

    fun setDirection(dir: TextDirectionMode) = _editorState.update { it.copy(direction = dir) }
    fun setOrientation(orient: PageOrientation) = _editorState.update { it.copy(orientation = orient) }
    fun setMargins(margins: PageMargins) = _editorState.update { it.copy(margins = margins) }
    fun setFontFamily(font: String) = _editorState.update { it.copy(fontFamily = font) }
    fun setFontSize(sizeSp: Int) = _editorState.update { it.copy(fontSizeSp = sizeSp.coerceIn(8, 32)) }
    fun setWatermark(text: String) = _editorState.update { it.copy(watermark = text.trim().take(80)) }
    fun setZoom(zoom: Int) = _editorState.update { it.copy(zoomPercent = zoom.coerceIn(75, 150)) }
    fun toggleRuler() = _editorState.update { it.copy(showRuler = !it.showRuler) }
    fun setViewMode(mode: EditorViewMode) = _editorState.update { it.copy(viewMode = mode) }
    fun setRibbonTab(tab: RibbonTab) = _editorState.update { it.copy(activeRibbonTab = tab) }
    fun dismissOutOfCreditsDialog() = _editorState.update { it.copy(showOutOfCreditsDialog = false) }

    fun findAndReplace(findText: String, replaceText: String, matchCase: Boolean): Int {
        if (findText.isEmpty()) return 0
        val current = _editorState.value.markdown
        val regex = Regex(Regex.escape(findText), if (matchCase) emptySet() else setOf(RegexOption.IGNORE_CASE))
        val matches = regex.findAll(current).count()
        if (matches > 0) {
            val updated = regex.replace(current) { replaceText }
            updateMarkdown(updated, recordHistory = true)
            emitToast(
                if (language.value == SupportedLanguage.AR) "تم استبدال $matches موضعاً" else "Replaced $matches occurrences"
            )
        } else {
            emitToast(
                if (language.value == SupportedLanguage.AR) "لم يتم العثور على نتائج مطابقة" else "No matches found"
            )
        }
        return matches
    }

    fun saveCurrentDocument() {
        val state = _editorState.value
        if (state.isSaving) return
        viewModelScope.launch {
            _editorState.update { it.copy(isSaving = true) }
            val result = repository.saveDocument(
                userId = currentUserId,
                docId = state.currentDocId,
                title = state.title,
                markdown = state.markdown,
                direction = state.direction.wireValue,
                orientation = state.orientation.wireValue,
                margins = state.margins.wireValue,
                fontFamily = state.fontFamily,
                fontSizeSp = state.fontSizeSp.toLong(),
                watermark = state.watermark
            )
            _editorState.update { it.copy(isSaving = false) }
            result.fold(
                onSuccess = { docId ->
                    _editorState.update { it.copy(currentDocId = docId) }
                    emitToast(AppStringsProvider.forLanguage(language.value).saveSuccess)
                },
                onFailure = { err ->
                    emitToast(err.localizedMessage ?: "Save failed")
                }
            )
        }
    }

    fun loadSavedDocument(doc: SavedDocument) {
        _editorState.update { state ->
            state.copy(
                currentDocId = doc.id,
                title = doc.title,
                markdown = doc.markdown,
                direction = TextDirectionMode.fromWire(doc.direction),
                orientation = PageOrientation.fromWire(doc.orientation),
                margins = PageMargins.fromWire(doc.margins),
                fontFamily = doc.fontFamily.ifBlank { "Cairo" },
                fontSizeSp = doc.fontSizeSp.toInt().coerceIn(8, 32),
                watermark = doc.watermark,
                undoStack = emptyList(),
                redoStack = emptyList()
            )
        }
    }

    fun deleteSavedDocument(docId: String) {
        viewModelScope.launch {
            val res = repository.deleteDocument(currentUserId, docId)
            res.onSuccess {
                if (_editorState.value.currentDocId == docId) {
                    _editorState.update { it.copy(currentDocId = null) }
                }
                emitToast(if (language.value == SupportedLanguage.AR) "تم حذف المستند" else "Document deleted")
            }.onFailure { err ->
                emitToast(err.localizedMessage ?: "Delete failed")
            }
        }
    }

    fun fetchAndInsertShareLink(url: String, replaceEntireDoc: Boolean) {
        if (_editorState.value.isFetchingShareLink) return
        viewModelScope.launch {
            _editorState.update { it.copy(isFetchingShareLink = true) }
            val result = ShareLinkFetcher.fetchShareLinkMarkdown(url)
            _editorState.update { it.copy(isFetchingShareLink = false) }
            result.fold(
                onSuccess = { md ->
                    if (replaceEntireDoc) {
                        replaceDocumentContent("AI Conversation", md)
                    } else {
                        insertSnippet(md)
                    }
                    emitToast(
                        if (language.value == SupportedLanguage.AR) "تم جلب ومعالجة الرابط" else "Share link imported"
                    )
                },
                onFailure = { err ->
                    emitToast(err.localizedMessage ?: "Invalid share URL")
                }
            )
        }
    }

    fun loadTemplateIntoEditor(template: TemplateItem) {
        val lang = language.value
        val currentState = _editorState.value
        // Auto-save backup if there is substantial existing work that hasn't been synced yet
        if (currentState.markdown.trim().length > 80 && currentState.currentDocId == null) {
            viewModelScope.launch {
                repository.saveDocument(
                    userId = currentUserId,
                    docId = null,
                    title = "${currentState.title} (Auto Backup)",
                    markdown = currentState.markdown,
                    direction = currentState.direction.wireValue,
                    orientation = currentState.orientation.wireValue,
                    margins = currentState.margins.wireValue,
                    fontFamily = currentState.fontFamily,
                    fontSizeSp = currentState.fontSizeSp.toLong(),
                    watermark = currentState.watermark
                )
            }
        }
        val tplName = template.pickName(lang)
        val tplMarkdown = template.pickMarkdown(lang)
        val tplDir = if (lang == SupportedLanguage.AR) TextDirectionMode.RTL else TextDirectionMode.LTR
        replaceDocumentContent(tplName, tplMarkdown, tplDir)
        emitToast(if (lang == SupportedLanguage.AR) "تم فتح القالب في المحرر" else "Template loaded into editor")
    }

    fun importDocxResult(result: ImportedDocxResult) {
        if (result.markdown.isBlank()) {
            emitToast(
                if (language.value == SupportedLanguage.AR) "تعذر استخراج نص من ملف Word" else "Could not extract content from DOCX"
            )
            return
        }
        replaceDocumentContent(result.title, result.markdown)
        emitToast(
            if (language.value == SupportedLanguage.AR) "تم استيراد ملف Word بنجاح" else "DOCX imported into editor"
        )
    }

    fun buildCurrentDocxBytes(): ByteArray {
        val state = _editorState.value
        return DocxExporter.markdownToDocxBytes(
            markdown = state.markdown,
            options = DocxExportOptions(
                orientation = state.orientation,
                margins = state.margins,
                fontFamily = state.fontFamily,
                fontSizePt = state.fontSizeSp.coerceIn(9, 20),
                direction = state.direction,
                watermark = state.watermark
            )
        )
    }

    fun buildTemplateDocxBytes(template: TemplateItem): ByteArray {
        val lang = language.value
        val dir = if (lang == SupportedLanguage.AR) TextDirectionMode.RTL else TextDirectionMode.LTR
        return DocxExporter.markdownToDocxBytes(
            markdown = template.pickMarkdown(lang),
            options = DocxExportOptions(
                direction = dir,
                fontFamily = if (lang == SupportedLanguage.AR) "Cairo" else "Plus Jakarta Sans"
            )
        )
    }

    suspend fun tryConsumeExportCredit(): Boolean {
        val profile = (userProfileState.value as? UiState.Success)?.data
        val currentCredits = profile?.credits ?: 0L
        if (currentCredits <= 0L) {
            _editorState.update { it.copy(showOutOfCreditsDialog = true) }
            return false
        }
        val res = repository.deductCredit(currentUserId, currentCredits)
        if (res.isSuccess) {
            emitToast(AppStringsProvider.forLanguage(language.value).creditDeducted)
        }
        return true
    }

    fun topUpCredits(amount: Long) {
        val profile = (userProfileState.value as? UiState.Success)?.data
        val cur = profile?.credits ?: 0L
        val max = profile?.maxCredits ?: 25L
        viewModelScope.launch {
            val res = repository.addCredits(currentUserId, cur, max, amount)
            res.onSuccess {
                emitToast(
                    if (language.value == SupportedLanguage.AR) "تمت إضافة $amount نقطة إلى رصيدك"
                    else "Added $amount credits to your account"
                )
            }.onFailure { err ->
                emitToast(err.localizedMessage ?: "Failed to update credits")
            }
        }
    }

    fun toggleFavoriteTemplate(templateId: String) {
        val profile = (userProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.toggleFavoriteTemplate(currentUserId, templateId, profile.favoriteTemplateIds)
        }
    }

    fun setLanguage(lang: SupportedLanguage) {
        viewModelScope.launch {
            prefsStore.setLanguage(lang)
            repository.updatePreferences(currentUserId, lang, darkMode.value)
        }
    }

    fun setDarkMode(dark: Boolean) {
        viewModelScope.launch {
            prefsStore.setDarkMode(dark)
            repository.updatePreferences(currentUserId, language.value, dark)
        }
    }

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            val res = repository.updateDisplayName(currentUserId, newName)
            res.onSuccess {
                emitToast(if (language.value == SupportedLanguage.AR) "تم تحديث الاسم" else "Display name updated")
            }.onFailure { err ->
                emitToast(err.localizedMessage ?: "Update failed")
            }
        }
    }

    fun emitToast(message: String) {
        _snackbarEvents.tryEmit(message)
    }

    private companion object {
        const val TAG = "MainViewModel"
    }
}
