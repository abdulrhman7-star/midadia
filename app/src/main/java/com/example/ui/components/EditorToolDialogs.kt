package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.i18n.AppStrings
import com.example.data.model.DocumentStats
import com.example.data.model.SavedDocument
import com.example.data.model.SupportedLanguage
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SymbolPaletteDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onInsertSymbol: (String) -> Unit
) {
    val categories = listOf(
        "Math & Logic" to listOf("±", "×", "÷", "√", "∫", "∑", "∞", "≈", "≠", "≤", "≥", "∀", "∃", "∈", "∉"),
        "Greek" to listOf("α", "β", "γ", "δ", "θ", "λ", "μ", "π", "σ", "φ", "ψ", "ω", "Δ", "Σ", "Ω"),
        "Arrows" to listOf("→", "←", "↔", "⇒", "⇐", "⇔", "↑", "↓"),
        "Legal & Marks" to listOf("§", "¶", "©", "®", "™", "°", "‰", "✓", "✗", "★", "•")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.symbolInsert) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.forEach { (categoryName, symbols) ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            symbols.forEach { sym ->
                                OutlinedButton(
                                    onClick = {
                                        onInsertSymbol(sym)
                                        onDismiss()
                                    },
                                    modifier = Modifier.size(width = 48.dp, height = 48.dp)
                                ) {
                                    Text(sym, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WatermarkDialog(
    strings: AppStrings,
    currentWatermark: String,
    language: SupportedLanguage,
    onDismiss: () -> Unit,
    onApplyWatermark: (String) -> Unit
) {
    val isAr = language == SupportedLanguage.AR
    var text by remember { mutableStateOf(currentWatermark) }
    val presets = listOf("CONFIDENTIAL", "DRAFT", "URGENT", "سري للغاية", "مسودة")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.watermarkTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { p ->
                        FilterChip(
                            selected = text == p,
                            onClick = { text = p },
                            label = { Text(p) }
                        )
                    }
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(80) },
                    singleLine = true,
                    label = { Text(if (isAr) "نص العلامة المائية" else "Watermark Text") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("watermark_text_input")
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentWatermark.isNotBlank()) {
                    TextButton(onClick = {
                        onApplyWatermark("")
                        onDismiss()
                    }) {
                        Text(if (isAr) "إزالة" else "Clear")
                    }
                }
                Button(
                    onClick = {
                        onApplyWatermark(text)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("confirm_watermark")
                ) {
                    Text(strings.confirm)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

@Composable
fun FindReplaceDialog(
    strings: AppStrings,
    language: SupportedLanguage,
    currentMarkdown: String,
    onDismiss: () -> Unit,
    onReplaceAll: (String, String, Boolean) -> Unit
) {
    val isAr = language == SupportedLanguage.AR
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var matchCase by remember { mutableStateOf(false) }

    val matchCount = remember(findText, matchCase, currentMarkdown) {
        if (findText.isEmpty()) 0
        else Regex(
            Regex.escape(findText),
            if (matchCase) emptySet() else setOf(RegexOption.IGNORE_CASE)
        ).findAll(currentMarkdown).count()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.findReplaceTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    singleLine = true,
                    label = { Text(if (isAr) "البحث عن" else "Find") },
                    supportingText = {
                        if (findText.isNotEmpty()) {
                            Text(if (isAr) "النتائج المطابقة: $matchCount" else "$matchCount matches found")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("find_text_input")
                )
                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    singleLine = true,
                    label = { Text(if (isAr) "استبدال بـ" else "Replace with") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("replace_text_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isAr) "مطابقة حالة الأحرف" else "Match Case", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = matchCase, onCheckedChange = { matchCase = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onReplaceAll(findText, replaceText, matchCase)
                    onDismiss()
                },
                enabled = findText.isNotEmpty() && matchCount > 0,
                modifier = Modifier.testTag("confirm_replace_all")
            ) {
                Text(if (isAr) "استبدال الكل ($matchCount)" else "Replace All ($matchCount)")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.close) } }
    )
}

@Composable
fun DocumentStatsDialog(
    strings: AppStrings,
    stats: DocumentStats,
    language: SupportedLanguage,
    onDismiss: () -> Unit
) {
    val isAr = language == SupportedLanguage.AR
    val rows = listOf(
        (if (isAr) "الكلمات" else "Words") to "${stats.words}",
        (if (isAr) "الأحرف (مع المسافات)" else "Characters (with spaces)") to "${stats.charsWithSpaces}",
        (if (isAr) "الأحرف (بدون مسافات)" else "Characters (no spaces)") to "${stats.charsNoSpaces}",
        (if (isAr) "الفقرات" else "Paragraphs") to "${stats.paragraphs}",
        (if (isAr) "الجمل" else "Sentences") to "${stats.sentences}",
        (if (isAr) "الأسطر" else "Lines") to "${stats.lines}",
        (if (isAr) "وقت القراءة المقدر" else "Est. Reading Time") to "${stats.readingMinutes} min",
        (if (isAr) "وقت الإلقاء المقدر" else "Est. Speaking Time") to "${stats.speakingMinutes} min",
        (if (isAr) "المعادلات الرياضية (LaTeX)" else "Math Equations (LaTeX)") to "${stats.mathCount}",
        (if (isAr) "الكتل البرمجية" else "Code Blocks") to "${stats.codeBlockCount}"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.statsTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.testTag("close_stats_dialog")) {
                Text(strings.close)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedDocumentsSheet(
    strings: AppStrings,
    documents: List<SavedDocument>,
    language: SupportedLanguage,
    onDismiss: () -> Unit,
    onOpenDocument: (SavedDocument) -> Unit,
    onDeleteDocument: (String) -> Unit
) {
    val isAr = language == SupportedLanguage.AR
    var confirmDeleteDoc by remember { mutableStateOf<SavedDocument?>(null) }
    val dateFmt = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = strings.savedDocsTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            if (documents.isEmpty()) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = strings.noSavedDocs,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(documents, key = { it.id }) { doc ->
                        val ts = doc.updatedAt ?: doc.createdAt ?: Timestamp.now()
                        Card(
                            onClick = {
                                onOpenDocument(doc)
                                onDismiss()
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("saved_doc_item_${doc.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doc.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${doc.direction.uppercase()} · ${doc.fontFamily} · ${dateFmt.format(ts.toDate())}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { confirmDeleteDoc = doc },
                                    modifier = Modifier.testTag("delete_saved_doc_${doc.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = if (isAr) "حذف" else "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }

    confirmDeleteDoc?.let { target ->
        AlertDialog(
            onDismissRequest = { confirmDeleteDoc = null },
            title = { Text(target.title) },
            text = { Text(strings.deleteConfirm) },
            confirmButton = {
                Button(onClick = {
                    onDeleteDocument(target.id)
                    confirmDeleteDoc = null
                }) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteDoc = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
fun OutOfCreditsDialog(
    strings: AppStrings,
    language: SupportedLanguage,
    onDismiss: () -> Unit,
    onTopUpCredits: (Long) -> Unit
) {
    val isAr = language == SupportedLanguage.AR
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.outOfCreditsTitle) },
        text = { Text(strings.outOfCreditsDesc) },
        confirmButton = {
            Button(
                onClick = {
                    onTopUpCredits(25L)
                    onDismiss()
                },
                modifier = Modifier.testTag("quick_topup_25_button")
            ) {
                Text(if (isAr) "+25 نقطة مجانية للتجربة" else "+25 Starter Credits")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.close) }
        }
    )
}
