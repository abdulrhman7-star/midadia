package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatAlignLeft
import androidx.compose.material.icons.automirrored.rounded.FormatAlignRight
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.FindReplace
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.FormatStrikethrough
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.HorizontalRule
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.InsertPageBreak
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.StayCurrentLandscape
import androidx.compose.material.icons.rounded.StayCurrentPortrait
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.i18n.AppStrings
import com.example.data.model.EditorViewMode
import com.example.data.model.PageMargins
import com.example.data.model.PageOrientation
import com.example.data.model.RibbonTab
import com.example.data.model.SupportedLanguage
import com.example.data.model.TextDirectionMode
import com.example.ui.viewmodel.EditorUiState

@Composable
fun WordRibbon(
    state: EditorUiState,
    strings: AppStrings,
    language: SupportedLanguage,
    onSelectTab: (RibbonTab) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onWrapFormat: (String, String, String) -> Unit,
    onInsertSnippet: (String) -> Unit,
    onFontChange: (String) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onDirectionChange: (TextDirectionMode) -> Unit,
    onOrientationChange: (PageOrientation) -> Unit,
    onMarginsChange: (PageMargins) -> Unit,
    onViewModeChange: (EditorViewMode) -> Unit,
    onToggleRuler: () -> Unit,
    onZoomChange: (Int) -> Unit,
    onOpenInsertAiDialog: () -> Unit,
    onOpenTableDialog: () -> Unit,
    onOpenMathDialog: () -> Unit,
    onOpenCodeDialog: () -> Unit,
    onOpenImageDialog: () -> Unit,
    onOpenLinkDialog: () -> Unit,
    onOpenSymbolDialog: () -> Unit,
    onOpenWatermarkDialog: () -> Unit,
    onOpenFindReplaceDialog: () -> Unit,
    onOpenStatsDialog: () -> Unit,
    onSaveDoc: () -> Unit,
    onOpenSavedDocs: () -> Unit,
    onClearDoc: () -> Unit,
    onImportDocx: () -> Unit,
    onExportDocx: () -> Unit,
    onExportPdf: () -> Unit,
    onCopyGoogleDocs: () -> Unit
) {
    val isAr = language == SupportedLanguage.AR
    val tabs = listOf(
        RibbonTab.HOME to strings.ribbonHome,
        RibbonTab.INSERT to strings.ribbonInsert,
        RibbonTab.LAYOUT to strings.ribbonLayout,
        RibbonTab.REVIEW to strings.ribbonReview,
        RibbonTab.VIEW to strings.ribbonView,
        RibbonTab.FILE to strings.ribbonFile
    )
    val selectedIdx = tabs.indexOfFirst { it.first == state.activeRibbonTab }.coerceAtLeast(0)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ScrollableTabRow(
                selectedTabIndex = selectedIdx,
                edgePadding = 8.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                tabs.forEach { (tab, label) ->
                    Tab(
                        selected = tab == state.activeRibbonTab,
                        onClick = { onSelectTab(tab) },
                        modifier = Modifier.testTag("ribbon_tab_${tab.name.lowercase()}"),
                        text = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (tab == state.activeRibbonTab) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (state.activeRibbonTab) {
                    RibbonTab.HOME -> {
                        RibbonActionChip(
                            icon = Icons.AutoMirrored.Rounded.Undo,
                            label = if (isAr) "تراجع" else "Undo",
                            enabled = state.undoStack.isNotEmpty(),
                            tag = "ribbon_undo",
                            onClick = onUndo
                        )
                        RibbonActionChip(
                            icon = Icons.AutoMirrored.Rounded.Redo,
                            label = if (isAr) "إعادة" else "Redo",
                            enabled = state.redoStack.isNotEmpty(),
                            tag = "ribbon_redo",
                            onClick = onRedo
                        )
                        RibbonFontSelector(
                            selectedFont = state.fontFamily,
                            onSelectFont = onFontChange
                        )
                        RibbonFontSizeStepper(
                            fontSizeSp = state.fontSizeSp,
                            onFontSizeChange = onFontSizeChange
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.FormatBold,
                            label = "B",
                            tag = "ribbon_bold",
                            onClick = { onWrapFormat("**", "**", if (isAr) "نص عريض" else "bold text") }
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.FormatItalic,
                            label = "I",
                            tag = "ribbon_italic",
                            onClick = { onWrapFormat("*", "*", if (isAr) "نص مائل" else "italic text") }
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.FormatStrikethrough,
                            label = "S",
                            tag = "ribbon_strike",
                            onClick = { onWrapFormat("~~", "~~", if (isAr) "نص مشطوب" else "strikethrough") }
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.Code,
                            label = "`Code`",
                            tag = "ribbon_inline_code",
                            onClick = { onWrapFormat("`", "`", "code") }
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.Title,
                            label = "H1",
                            tag = "ribbon_h1",
                            onClick = { onInsertSnippet("# ${if (isAr) "عنوان رئيسي" else "Heading 1"}") }
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.Title,
                            label = "H2",
                            tag = "ribbon_h2",
                            onClick = { onInsertSnippet("## ${if (isAr) "عنوان فرعي" else "Heading 2"}") }
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.Title,
                            label = "H3",
                            tag = "ribbon_h3",
                            onClick = { onInsertSnippet("### ${if (isAr) "عنوان ثالث" else "Heading 3"}") }
                        )
                        RibbonActionChip(
                            icon = Icons.AutoMirrored.Rounded.FormatListBulleted,
                            label = if (isAr) "قائمة" else "Bullets",
                            tag = "ribbon_ul",
                            onClick = { onInsertSnippet("- ${if (isAr) "بند جديد" else "List item"}") }
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.FormatListNumbered,
                            label = if (isAr) "مرقمة" else "Numbered",
                            tag = "ribbon_ol",
                            onClick = { onInsertSnippet("1. ${if (isAr) "البند الأول" else "First item"}") }
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.CheckBox,
                            label = if (isAr) "مهام" else "Tasks",
                            tag = "ribbon_tasks",
                            onClick = { onInsertSnippet("- [ ] ${if (isAr) "مهمة للمتابعة" else "Action item"}") }
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.FormatQuote,
                            label = if (isAr) "اقتباس" else "Quote",
                            tag = "ribbon_quote",
                            onClick = { onInsertSnippet("> ${if (isAr) "نص الاقتباس" else "Quoted note"}") }
                        )
                    }

                    RibbonTab.INSERT -> {
                        RibbonActionChip(
                            icon = Icons.Rounded.AutoAwesome,
                            label = if (isAr) "محتوى AI" else "AI / Link",
                            tag = "ribbon_insert_ai",
                            onClick = onOpenInsertAiDialog
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.TableChart,
                            label = if (isAr) "جدول" else "Table",
                            tag = "ribbon_insert_table",
                            onClick = onOpenTableDialog
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.Functions,
                            label = if (isAr) "معادلة" else "Math",
                            tag = "ribbon_insert_math",
                            onClick = onOpenMathDialog
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.Code,
                            label = if (isAr) "كود" else "Code",
                            tag = "ribbon_insert_code",
                            onClick = onOpenCodeDialog
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.Image,
                            label = if (isAr) "صورة" else "Image",
                            tag = "ribbon_insert_image",
                            onClick = onOpenImageDialog
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.Link,
                            label = if (isAr) "رابط" else "Link",
                            tag = "ribbon_insert_link",
                            onClick = onOpenLinkDialog
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.EditNote,
                            label = if (isAr) "رموز" else "Symbols",
                            tag = "ribbon_insert_symbols",
                            onClick = onOpenSymbolDialog
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.HorizontalRule,
                            label = if (isAr) "فاصل" else "Divider",
                            tag = "ribbon_insert_hr",
                            onClick = { onInsertSnippet("---") }
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.InsertPageBreak,
                            label = if (isAr) "صفحة" else "Page Break",
                            tag = "ribbon_insert_pagebreak",
                            onClick = { onInsertSnippet("<!-- pagebreak -->") }
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.Description,
                            label = if (isAr) "ملاحظة" else "Note Box",
                            tag = "ribbon_insert_note",
                            onClick = {
                                onInsertSnippet(
                                    "> [!NOTE]\n> ${if (isAr) "أضف ملاحظة توضيحية هنا." else "Add an informational callout here."}"
                                )
                            }
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.Warning,
                            label = if (isAr) "تنبيه" else "Warning",
                            tag = "ribbon_insert_warning",
                            onClick = {
                                onInsertSnippet(
                                    "> [!WARNING]\n> ${if (isAr) "تنبيه مهم للمراجعة قبل الطباعة." else "Important warning to verify before exporting."}"
                                )
                            }
                        )
                    }

                    RibbonTab.LAYOUT -> {
                        FilterChip(
                            selected = state.direction == TextDirectionMode.RTL,
                            onClick = { onDirectionChange(TextDirectionMode.RTL) },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Rounded.FormatAlignRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text(if (isAr) "عربي (RTL)" else "RTL") },
                            modifier = Modifier.testTag("layout_rtl")
                        )
                        FilterChip(
                            selected = state.direction == TextDirectionMode.LTR,
                            onClick = { onDirectionChange(TextDirectionMode.LTR) },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Rounded.FormatAlignLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text("LTR") },
                            modifier = Modifier.testTag("layout_ltr")
                        )
                        FilterChip(
                            selected = state.orientation == PageOrientation.PORTRAIT,
                            onClick = { onOrientationChange(PageOrientation.PORTRAIT) },
                            leadingIcon = {
                                Icon(Icons.Rounded.StayCurrentPortrait, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text(if (isAr) "عمودي" else "Portrait") },
                            modifier = Modifier.testTag("layout_portrait")
                        )
                        FilterChip(
                            selected = state.orientation == PageOrientation.LANDSCAPE,
                            onClick = { onOrientationChange(PageOrientation.LANDSCAPE) },
                            leadingIcon = {
                                Icon(Icons.Rounded.StayCurrentLandscape, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text(if (isAr) "أفقي" else "Landscape") },
                            modifier = Modifier.testTag("layout_landscape")
                        )
                        FilterChip(
                            selected = state.margins == PageMargins.NORMAL,
                            onClick = { onMarginsChange(PageMargins.NORMAL) },
                            label = { Text(if (isAr) "هامش عادي" else "Normal") },
                            modifier = Modifier.testTag("layout_margin_normal")
                        )
                        FilterChip(
                            selected = state.margins == PageMargins.NARROW,
                            onClick = { onMarginsChange(PageMargins.NARROW) },
                            label = { Text(if (isAr) "ضيق" else "Narrow") },
                            modifier = Modifier.testTag("layout_margin_narrow")
                        )
                        FilterChip(
                            selected = state.margins == PageMargins.WIDE,
                            onClick = { onMarginsChange(PageMargins.WIDE) },
                            label = { Text(if (isAr) "عريض" else "Wide") },
                            modifier = Modifier.testTag("layout_margin_wide")
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.WaterDrop,
                            label = if (isAr) "علامة مائية" else "Watermark",
                            tag = "layout_watermark",
                            onClick = onOpenWatermarkDialog
                        )
                    }

                    RibbonTab.REVIEW -> {
                        RibbonActionChip(
                            icon = Icons.Rounded.FindReplace,
                            label = strings.findReplaceTitle,
                            tag = "review_find_replace",
                            onClick = onOpenFindReplaceDialog
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.Analytics,
                            label = strings.statsTitle,
                            tag = "review_stats",
                            onClick = onOpenStatsDialog
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.CloudDone,
                            label = strings.saveDoc,
                            tag = "review_save",
                            onClick = onSaveDoc
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.FolderOpen,
                            label = strings.savedDocsTitle,
                            tag = "review_saved_docs",
                            onClick = onOpenSavedDocs
                        )
                    }

                    RibbonTab.VIEW -> {
                        FilterChip(
                            selected = state.viewMode == EditorViewMode.SPLIT,
                            onClick = { onViewModeChange(EditorViewMode.SPLIT) },
                            leadingIcon = { Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text(strings.viewSplit) },
                            modifier = Modifier.testTag("view_split")
                        )
                        FilterChip(
                            selected = state.viewMode == EditorViewMode.PRINT,
                            onClick = { onViewModeChange(EditorViewMode.PRINT) },
                            label = { Text(strings.viewPrint) },
                            modifier = Modifier.testTag("view_print")
                        )
                        FilterChip(
                            selected = state.viewMode == EditorViewMode.SOURCE,
                            onClick = { onViewModeChange(EditorViewMode.SOURCE) },
                            label = { Text(strings.viewSource) },
                            modifier = Modifier.testTag("view_source")
                        )
                        FilterChip(
                            selected = state.showRuler,
                            onClick = onToggleRuler,
                            leadingIcon = { Icon(Icons.Rounded.Straighten, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text(if (isAr) "المسطرة" else "Ruler") },
                            modifier = Modifier.testTag("view_ruler_toggle")
                        )
                        listOf(75, 100, 125, 150).forEach { zoomVal ->
                            FilterChip(
                                selected = state.zoomPercent == zoomVal,
                                onClick = { onZoomChange(zoomVal) },
                                label = { Text("$zoomVal%") },
                                modifier = Modifier.testTag("view_zoom_$zoomVal")
                            )
                        }
                    }

                    RibbonTab.FILE -> {
                        RibbonActionChip(
                            icon = Icons.Rounded.FileUpload,
                            label = if (isAr) "استيراد DOCX" else "Import DOCX",
                            tag = "file_import_docx",
                            onClick = onImportDocx
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.Download,
                            label = strings.exportDocx,
                            tag = "file_export_docx",
                            onClick = onExportDocx
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.PictureAsPdf,
                            label = strings.exportPdf,
                            tag = "file_export_pdf",
                            onClick = onExportPdf
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.ContentCopy,
                            label = strings.copyGDocs,
                            tag = "file_copy_gdocs",
                            onClick = onCopyGoogleDocs
                        )
                        RibbonActionChip(
                            icon = Icons.Rounded.DeleteSweep,
                            label = strings.clearDoc,
                            tag = "file_clear_doc",
                            onClick = onClearDoc
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RibbonActionChip(
    icon: ImageVector,
    label: String,
    tag: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = Modifier
            .heightIn(min = 40.dp)
            .testTag(tag),
        shape = MaterialTheme.shapes.small
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun RibbonFontSelector(
    selectedFont: String,
    onSelectFont: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val fonts = listOf("Cairo", "Amiri", "Plus Jakarta Sans", "JetBrains Mono")
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier
                .heightIn(min = 40.dp)
                .testTag("ribbon_font_selector"),
            shape = MaterialTheme.shapes.small
        ) {
            Text(text = selectedFont, style = MaterialTheme.typography.labelMedium)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            fonts.forEach { font ->
                DropdownMenuItem(
                    text = { Text(font) },
                    onClick = {
                        onSelectFont(font)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RibbonFontSizeStepper(
    fontSizeSp: Int,
    onFontSizeChange: (Int) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            TextButton(
                onClick = { onFontSizeChange(fontSizeSp - 1) },
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.testTag("ribbon_font_minus")
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Decrease font size", modifier = Modifier.size(16.dp))
            }
            Text(
                text = "${fontSizeSp}px",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            TextButton(
                onClick = { onFontSizeChange(fontSizeSp + 1) },
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.testTag("ribbon_font_plus")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Increase font size", modifier = Modifier.size(16.dp))
            }
        }
    }
}
