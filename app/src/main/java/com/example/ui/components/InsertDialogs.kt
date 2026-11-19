package com.example.ui.components

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.i18n.AppStrings
import com.example.data.model.SupportedLanguage
import com.example.domain.markdown.MarkdownToHtml
import java.io.ByteArrayOutputStream

@Composable
fun InsertAiContentDialog(
    strings: AppStrings,
    language: SupportedLanguage,
    isFetchingShareLink: Boolean,
    onDismiss: () -> Unit,
    onInsertMarkdown: (String, Boolean) -> Unit,
    onFetchShareLink: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val isAr = language == SupportedLanguage.AR
    var selectedTab by remember { mutableIntStateOf(0) }
    var rawMarkdown by remember { mutableStateOf("") }
    var shareUrl by remember { mutableStateOf("") }
    var replaceEntire by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.insertButton, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(strings.tabRawMarkdown, style = MaterialTheme.typography.labelMedium) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(strings.tabShareLink, style = MaterialTheme.typography.labelMedium) }
                    )
                }

                if (selectedTab == 0) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clipText = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
                            if (!clipText.isNullOrBlank()) {
                                rawMarkdown = clipText
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("insert_paste_clipboard")
                    ) {
                        Icon(Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.pasteClipboard)
                    }

                    Text(
                        text = strings.presetsLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MarkdownToHtml.SAMPLE_MARKDOWN_PRESETS.forEach { preset ->
                            FilterChip(
                                selected = false,
                                onClick = { rawMarkdown = preset.markdown },
                                label = { Text(if (isAr) preset.titleAr else preset.titleEn) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = rawMarkdown,
                        onValueChange = { rawMarkdown = it },
                        minLines = 5,
                        maxLines = 8,
                        placeholder = { Text(strings.editorPlaceholder) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("insert_raw_markdown_input")
                    )
                } else {
                    OutlinedTextField(
                        value = shareUrl,
                        onValueChange = { shareUrl = it },
                        singleLine = true,
                        placeholder = { Text(strings.shareLinkPlaceholder) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("insert_share_url_input")
                    )
                    Text(
                        text = if (isAr) {
                            "يدعم روابط مشاركة ChatGPT وGemini وClaude العامة."
                        } else {
                            "Supports public ChatGPT, Gemini, and Claude conversation share URLs."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.replaceAction, style = MaterialTheme.typography.bodySmall)
                    Switch(checked = replaceEntire, onCheckedChange = { replaceEntire = it })
                }
            }
        },
        confirmButton = {
            if (selectedTab == 0) {
                Button(
                    onClick = {
                        if (rawMarkdown.isNotBlank()) {
                            onInsertMarkdown(rawMarkdown, replaceEntire)
                            onDismiss()
                        }
                    },
                    enabled = rawMarkdown.isNotBlank(),
                    modifier = Modifier.testTag("confirm_insert_markdown")
                ) {
                    Text(if (replaceEntire) strings.replaceAction else strings.insertAction)
                }
            } else {
                Button(
                    onClick = {
                        onFetchShareLink(shareUrl, replaceEntire)
                        onDismiss()
                    },
                    enabled = shareUrl.isNotBlank() && !isFetchingShareLink,
                    modifier = Modifier.testTag("confirm_fetch_share_link")
                ) {
                    if (isFetchingShareLink) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(if (isFetchingShareLink) strings.fetching else strings.fetchLinkAction)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
fun InsertTableDialog(
    strings: AppStrings,
    language: SupportedLanguage,
    onDismiss: () -> Unit,
    onInsertTable: (String) -> Unit
) {
    val isAr = language == SupportedLanguage.AR
    var rows by remember { mutableIntStateOf(3) }
    var cols by remember { mutableIntStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.tableInsert) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (isAr) "عدد الأعمدة: $cols" else "Columns: $cols")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (2..6).forEach { c ->
                        FilterChip(
                            selected = cols == c,
                            onClick = { cols = c },
                            label = { Text("$c") }
                        )
                    }
                }
                Text(if (isAr) "عدد الصفوف: $rows" else "Rows: $rows")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (2..6).forEach { r ->
                        FilterChip(
                            selected = rows == r,
                            onClick = { rows = r },
                            label = { Text("$r") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val header = (1..cols).joinToString(" | ", "| ", " |") {
                        if (isAr) "العمود $it" else "Column $it"
                    }
                    val sep = (1..cols).joinToString(" | ", "| ", " |") { ":---:" }
                    val body = (1..rows).joinToString("\n") { r ->
                        (1..cols).joinToString(" | ", "| ", " |") { c ->
                            if (isAr) "بيان $r-$c" else "Cell $r.$c"
                        }
                    }
                    onInsertTable("$header\n$sep\n$body")
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_insert_table")
            ) {
                Text(strings.insertAction)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

@Composable
fun InsertMathDialog(
    strings: AppStrings,
    language: SupportedLanguage,
    onDismiss: () -> Unit,
    onInsertMath: (String) -> Unit
) {
    val isAr = language == SupportedLanguage.AR
    var latex by remember { mutableStateOf("\\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}") }
    var isDisplayBlock by remember { mutableStateOf(true) }

    val quickSnippets = listOf(
        "Fraction" to "\\frac{a}{b}",
        "Integral" to "\\int_{0}^{\\infty} e^{-x^2} dx",
        "Sum" to "\\sum_{i=1}^{n} x_i",
        "Root" to "\\sqrt{x^2 + y^2}",
        "Bell State" to "|\\Phi^+\\rangle = \\frac{1}{\\sqrt{2}}(|00\\rangle + |11\\rangle)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.mathInsert) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSnippets.forEach { (label, snippet) ->
                        FilterChip(
                            selected = latex == snippet,
                            onClick = { latex = snippet },
                            label = { Text(label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = latex,
                    onValueChange = { latex = it },
                    minLines = 3,
                    label = { Text("LaTeX") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("insert_math_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isAr) "معادلة مستقلة ($$)" else "Display Block ($$)", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = isDisplayBlock, onCheckedChange = { isDisplayBlock = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = latex.trim()
                    if (clean.isNotEmpty()) {
                        val out = if (isDisplayBlock) "\$\$\n$clean\n\$\$" else "\$$clean\$"
                        onInsertMath(out)
                        onDismiss()
                    }
                },
                enabled = latex.isNotBlank(),
                modifier = Modifier.testTag("confirm_insert_math")
            ) {
                Text(strings.insertAction)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

@Composable
fun InsertCodeDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onInsertCode: (String) -> Unit
) {
    val languages = listOf("python", "kotlin", "typescript", "sql", "bash", "json")
    var selectedLang by remember { mutableStateOf("python") }
    var code by remember { mutableStateOf("def hello_midadd():\n    print(\"Ready for Word & PDF!\")") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.codeInsert) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    languages.forEach { l ->
                        FilterChip(
                            selected = selectedLang == l,
                            onClick = { selectedLang = l },
                            label = { Text(l) }
                        )
                    }
                }
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("insert_code_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank()) {
                        onInsertCode("```$selectedLang\n${code.trimEnd()}\n```")
                        onDismiss()
                    }
                },
                enabled = code.isNotBlank(),
                modifier = Modifier.testTag("confirm_insert_code")
            ) {
                Text(strings.insertAction)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

@Composable
fun InsertImageDialog(
    strings: AppStrings,
    language: SupportedLanguage,
    onDismiss: () -> Unit,
    onInsertImage: (String) -> Unit
) {
    val context = LocalContext.current
    val isAr = language == SupportedLanguage.AR
    var url by remember { mutableStateOf("") }
    var altText by remember { mutableStateOf(if (isAr) "صورة توضيحية" else "Figure") }

    // Play-compliant zero-permission Android Photo Picker
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val encoded = encodeImageUriToDataUri(context, uri)
            if (encoded != null) {
                url = encoded
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.imageInsert) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_gallery_image_button")
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isAr) "اختيار صورة من الجهاز (Photo Picker)" else "Pick Photo from Device")
                }

                OutlinedTextField(
                    value = if (url.startsWith("data:image")) "[Embedded Local Image Ready]" else url,
                    onValueChange = { if (!it.startsWith("[Embedded")) url = it },
                    singleLine = true,
                    label = { Text(if (isAr) "أو رابط الصورة HTTPS" else "Or Image URL (HTTPS)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = altText,
                    onValueChange = { altText = it },
                    singleLine = true,
                    label = { Text(if (isAr) "النص البديل" else "Caption / Alt Text") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanUrl = url.trim()
                    if (cleanUrl.isNotEmpty()) {
                        val safeAlt = altText.trim().ifBlank { "Image" }
                        onInsertImage("![$safeAlt]($cleanUrl)")
                        onDismiss()
                    }
                },
                enabled = url.isNotBlank(),
                modifier = Modifier.testTag("confirm_insert_image")
            ) {
                Text(strings.insertAction)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

@Composable
fun InsertLinkDialog(
    strings: AppStrings,
    language: SupportedLanguage,
    onDismiss: () -> Unit,
    onInsertLink: (String) -> Unit
) {
    val isAr = language == SupportedLanguage.AR
    var label by remember { mutableStateOf("") }
    var href by remember { mutableStateOf("https://") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.linkInsert) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    singleLine = true,
                    label = { Text(if (isAr) "نص الرابط" else "Link Label") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = href,
                    onValueChange = { href = it },
                    singleLine = true,
                    label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val safeUrl = href.trim()
                    val safeLabel = label.trim().ifBlank { safeUrl }
                    if (safeUrl.startsWith("http://") || safeUrl.startsWith("https://")) {
                        onInsertLink("[$safeLabel]($safeUrl)")
                        onDismiss()
                    }
                },
                enabled = href.startsWith("http://") || href.startsWith("https://"),
                modifier = Modifier.testTag("confirm_insert_link")
            ) {
                Text(strings.insertAction)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

private fun encodeImageUriToDataUri(context: Context, uri: android.net.Uri): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val original = BitmapFactory.decodeStream(stream) ?: return null
            val maxDim = 720
            val scale = minOf(1f, maxDim.toFloat() / maxOf(original.width, original.height).toFloat())
            val w = (original.width * scale).toInt().coerceAtLeast(1)
            val h = (original.height * scale).toInt().coerceAtLeast(1)
            val scaled = if (scale < 1f) Bitmap.createScaledBitmap(original, w, h, true) else original
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 78, baos)
            val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            "data:image/jpeg;base64,$b64"
        }
    } catch (_: Exception) {
        null
    }
}
