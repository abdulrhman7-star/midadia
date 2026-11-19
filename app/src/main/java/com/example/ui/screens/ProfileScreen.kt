package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.i18n.AppStringsProvider
import com.example.data.model.SavedDocument
import com.example.data.model.SupportedLanguage
import com.example.data.model.UserProfile
import com.example.ui.auth.AuthManager
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    profile: UserProfile?,
    savedDocuments: List<SavedDocument>,
    language: SupportedLanguage,
    isDark: Boolean,
    onOpenSavedDocument: (SavedDocument) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val strings = remember(language) { AppStringsProvider.forLanguage(language) }
    val isAr = language == SupportedLanguage.AR

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showPricingDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }

    val displayName = profile?.displayName ?: "User"
    val email = profile?.email ?: ""
    val credits = profile?.credits ?: 0L
    val maxCredits = (profile?.maxCredits ?: 25L).coerceAtLeast(1L)
    val creditFraction = (credits.toFloat() / maxCredits.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // User Profile Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = displayName,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (email.isNotBlank()) {
                        Text(email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(
                    onClick = { showEditNameDialog = true },
                    modifier = Modifier.testTag("profile_edit_name_button")
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit Name")
                }
            }
        }

        // Export Credits & Plans Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(6.dp))
                        Text(strings.creditsLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "$credits / $maxCredits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { creditFraction },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.topUpCredits(25L) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("topup_25_credits_button")
                    ) {
                        Text(if (isAr) "+25 نقطة" else "+25 Credits")
                    }
                    OutlinedButton(
                        onClick = { showPricingDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_plans_dialog_button")
                    ) {
                        Text(if (isAr) "الباقات والأسئلة" else "Plans & FAQ")
                    }
                }
            }
        }

        // Language & Appearance Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isAr) "اللغة والمظهر" else "Language & Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SupportedLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = lang == language,
                            onClick = { viewModel.setLanguage(lang) },
                            label = { Text("${lang.flag} ${lang.nativeName}") },
                            modifier = Modifier.testTag("profile_lang_${lang.code}")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.DarkMode, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isAr) "الوضع الداكن" else "Dark Mode", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = viewModel::setDarkMode,
                        modifier = Modifier.testTag("profile_dark_mode_switch")
                    )
                }
            }
        }

        // Cloud Documents Summary
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${strings.savedDocsTitle} (${savedDocuments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (savedDocuments.isEmpty()) {
                    Text(
                        text = strings.noSavedDocs,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    savedDocuments.take(5).forEach { doc ->
                        Surface(
                            onClick = { onOpenSavedDocument(doc) },
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(doc.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Support & Sign Out Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showContactDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("profile_contact_button")
            ) {
                Icon(Icons.Rounded.MailOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isAr) "تواصل معنا" else "Support")
            }

            Button(
                onClick = {
                    AuthManager.signOut(credentialManager = credentialManager, scope = scope)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.signOut)
            }
        }
    }

    if (showEditNameDialog) {
        var nameInput by remember { mutableStateOf(displayName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text(if (isAr) "تعديل الاسم" else "Edit Display Name") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it.take(80) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateDisplayName(nameInput)
                        showEditNameDialog = false
                    },
                    enabled = nameInput.trim().isNotEmpty()
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text(strings.cancel) }
            }
        )
    }

    if (showPricingDialog) {
        AlertDialog(
            onDismissRequest = { showPricingDialog = false },
            title = { Text(if (isAr) "خطط الرصيد والمميزات" else "Credit Packs & Features") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isAr) {
                            "• الباقة المجانية: 25 نقطة تصدير مجانية مع حفظ سحابي كامل ودعم المعادلات.\n" +
                                "• باقة الباحث (Pro): +100 نقطة تصدير، علامات مائية مخصصة، وأولوية تحويل الجداول.\n" +
                                "• التصدير بصيغة Google Docs مجاني بالكامل ولا يستهلك نقاطاً."
                        } else {
                            "• Free Tier: 25 export credits with cloud sync & native OMML equations.\n" +
                                "• Researcher Pack: +100 export credits and custom document watermarks.\n" +
                                "• Copy for Google Docs is always free and uses 0 credits."
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.topUpCredits(100L)
                    showPricingDialog = false
                }) {
                    Text(if (isAr) "تفعيل +100 نقطة" else "Claim +100 Credits")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPricingDialog = false }) { Text(strings.close) }
            }
        )
    }

    if (showContactDialog) {
        var subject by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        val isValid = subject.trim().length >= 3 && message.trim().length >= 10
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text(if (isAr) "إرسال رسالة للدعم" else "Contact Support") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        singleLine = true,
                        label = { Text(if (isAr) "الموضوع" else "Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        minLines = 3,
                        label = { Text(if (isAr) "الرسالة (10 أحرف على الأقل)" else "Message (min 10 chars)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@midadd.com")
                            putExtra(Intent.EXTRA_SUBJECT, subject.trim())
                            putExtra(Intent.EXTRA_TEXT, message.trim())
                        }
                        runCatching { context.startActivity(intent) }
                        viewModel.emitToast(if (isAr) "تم تجهيز رسالة الدعم" else "Support message prepared")
                        showContactDialog = false
                    },
                    enabled = isValid
                ) {
                    Text(if (isAr) "إرسال" else "Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = false }) { Text(strings.cancel) }
            }
        )
    }
}
