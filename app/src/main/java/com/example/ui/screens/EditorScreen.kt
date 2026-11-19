package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.i18n.AppStringsProvider
import com.example.data.model.EditorViewMode
import com.example.data.model.SavedDocument
import com.example.data.model.SupportedLanguage
import com.example.domain.export.AndroidExportHelper
import com.example.domain.markdown.DocxImporter
import com.example.domain.markdown.MarkdownToHtml
import com.example.ui.components.DocumentStatsDialog
import com.example.ui.components.FindReplaceDialog
import com.example.ui.components.InsertAiContentDialog
import com.example.ui.components.InsertCodeDialog
import com.example.ui.components.InsertImageDialog
import com.example.ui.components.InsertLinkDialog
import com.example.ui.components.InsertMathDialog
import com.example.ui.components.InsertTableDialog
import com.example.ui.components.OutOfCreditsDialog
import com.example.ui.components.SavedDocumentsSheet
import com.example.ui.components.SymbolPaletteDialog
import com.example.ui.components.WatermarkDialog
import com.example.ui.components.WordRibbon
import com.example.ui.components.WordRuler
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.viewmodel.EditorUiState
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    viewModel: MainViewModel,
    editorState: EditorUiState,
    savedDocuments: List<SavedDocument>,
    credits: Long,
    language: SupportedLanguage,
    isDark: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val strings = remember(language) { AppStringsProvider.forLanguage(language) }
    val isAr = language == SupportedLanguage.AR

    var showInsertAiDialog by remember { mutableStateOf(false) }
    var showTableDialog by remember { mutableStateOf(false) }
    var showMathDialog by remember { mutableStateOf(false) }
    var showCodeDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showSymbolDialog by remember { mutableStateOf(false) }
    var showWatermarkDialog by remember { mutableStateOf(false) }
    var showFindReplaceDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showSavedDocsSheet by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var pendingDocxBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Import .docx file launcher
    val importDocxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val displayName = uri.lastPathSegment ?: "Imported Document"
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val parsed = DocxImporter.importFromStream(stream, displayName)
                    viewModel.importDocxResult(parsed)
                }
            } catch (e: Exception) {
                viewModel.emitToast(e.localizedMessage ?: "Failed to import DOCX")
            }
        }
    }

    // Save .docx via Storage Access Framework launcher
    val saveDocxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
    ) { uri ->
        val bytes = pendingDocxBytes
        pendingDocxBytes = null
        if (uri != null && bytes != null) {
            AndroidExportHelper.writeDocxToUri(context, uri, bytes)
                .onSuccess { viewModel.emitToast(strings.exportSuccess) }
                .onFailure { viewModel.emitToast(it.localizedMessage ?: "Export failed") }
        }
    }

    val a4Html = remember(
        editorState.markdown,
        editorState.title,
        editorState.direction,
        editorState.orientation,
        editorState.margins,
        editorState.fontFamily,
        editorState.fontSizeSp,
        editorState.watermark,
        editorState.zoomPercent,
        isDark
    ) {
        MarkdownToHtml.buildA4DocumentHtml(
            markdown = editorState.markdown,
            title = editorState.title,
            direction = editorState.direction,
            orientation = editorState.orientation,
            margins = editorState.margins,
            fontFamily = editorState.fontFamily,
            fontSizeSp = editorState.fontSizeSp,
            watermark = editorState.watermark,
            zoomPercent = editorState.zoomPercent,
            darkSurface = isDark
        )
    }

    fun triggerExportDocx(saveToDevice: Boolean) {
        scope.launch {
            if (!viewModel.tryConsumeExportCredit()) return@launch
            val bytes = viewModel.buildCurrentDocxBytes()
            if (saveToDevice) {
                pendingDocxBytes = bytes
                saveDocxLauncher.launch("${AndroidExportHelper.sanitizeFileName(editorState.title)}.docx")
            } else {
                AndroidExportHelper.shareDocxFile(context, editorState.title, bytes)
                    .onFailure { viewModel.emitToast(it.localizedMessage ?: "Share failed") }
            }
        }
    }

    fun triggerExportPdf() {
        scope.launch {
            if (!viewModel.tryConsumeExportCredit()) return@launch
            val printHtml = MarkdownToHtml.buildA4DocumentHtml(
                markdown = editorState.markdown,
                title = editorState.title,
                direction = editorState.direction,
                orientation = editorState.orientation,
                margins = editorState.margins,
                fontFamily = editorState.fontFamily,
                fontSizeSp = editorState.fontSizeSp,
                watermark = editorState.watermark,
                zoomPercent = 100,
                darkSurface = false
            )
            AndroidExportHelper.printHtmlToPdf(
                context = context,
                title = editorState.title,
                html = printHtml,
                orientation = editorState.orientation,
                onJobStarted = { viewModel.emitToast(strings.exportSuccess) },
                onError = { viewModel.emitToast(it) }
            )
        }
    }

    fun triggerCopyGoogleDocs() {
        val html = MarkdownToHtml.prepareGoogleDocsHtml(editorState.markdown, editorState.direction)
        AndroidExportHelper.copyForGoogleDocs(context, editorState.markdown, html)
            .onSuccess { viewModel.emitToast(strings.copiedGDocs) }
            .onFailure { viewModel.emitToast(it.localizedMessage ?: "Copy failed") }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Action & Title Header Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editorState.title,
                        onValueChange = viewModel::updateTitle,
                        singleLine = true,
                        placeholder = { Text("Document Title") },
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("document_title_input")
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${strings.creditsLabel}: $credits",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.saveCurrentDocument() },
                        enabled = !editorState.isSaving,
                        modifier = Modifier.testTag("header_save_button")
                    ) {
                        Icon(
                            imageVector = if (editorState.currentDocId != null) Icons.Rounded.CheckCircle else Icons.Rounded.CloudDone,
                            contentDescription = strings.saveDoc,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { showSavedDocsSheet = true },
                        modifier = Modifier.testTag("header_saved_docs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = strings.savedDocsTitle
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Quick Export & AI Insert Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showInsertAiDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 42.dp)
                            .testTag("insert_ai_content_button")
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isAr) "إدراج AI / Markdown" else "Insert AI / Markdown")
                    }

                    Button(
                        onClick = { triggerExportDocx(saveToDevice = true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 42.dp)
                            .testTag("export_docx_button")
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("DOCX")
                    }

                    OutlinedButton(
                        onClick = { triggerExportDocx(saveToDevice = false) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 42.dp)
                            .testTag("share_docx_button")
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(if (isAr) "مشاركة" else "Share")
                    }

                    OutlinedButton(
                        onClick = { triggerExportPdf() },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 42.dp)
                            .testTag("export_pdf_button")
                    ) {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("PDF")
                    }

                    OutlinedButton(
                        onClick = { triggerCopyGoogleDocs() },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 42.dp)
                            .testTag("copy_gdocs_button")
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Google Docs")
                    }
                }
            }
        }

        // Ribbon Toolbar
        WordRibbon(
            state = editorState,
            strings = strings,
            language = language,
            onSelectTab = viewModel::setRibbonTab,
            onUndo = viewModel::undo,
            onRedo = viewModel::redo,
            onWrapFormat = viewModel::wrapOrAppendFormat,
            onInsertSnippet = viewModel::insertSnippet,
            onFontChange = viewModel::setFontFamily,
            onFontSizeChange = viewModel::setFontSize,
            onDirectionChange = viewModel::setDirection,
            onOrientationChange = viewModel::setOrientation,
            onMarginsChange = viewModel::setMargins,
            onViewModeChange = viewModel::setViewMode,
            onToggleRuler = viewModel::toggleRuler,
            onZoomChange = viewModel::setZoom,
            onOpenInsertAiDialog = { showInsertAiDialog = true },
            onOpenTableDialog = { showTableDialog = true },
            onOpenMathDialog = { showMathDialog = true },
            onOpenCodeDialog = { showCodeDialog = true },
            onOpenImageDialog = { showImageDialog = true },
            onOpenLinkDialog = { showLinkDialog = true },
            onOpenSymbolDialog = { showSymbolDialog = true },
            onOpenWatermarkDialog = { showWatermarkDialog = true },
            onOpenFindReplaceDialog = { showFindReplaceDialog = true },
            onOpenStatsDialog = { showStatsDialog = true },
            onSaveDoc = viewModel::saveCurrentDocument,
            onOpenSavedDocs = { showSavedDocsSheet = true },
            onClearDoc = { showClearConfirm = true },
            onImportDocx = {
                importDocxLauncher.launch(
                    arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                )
            },
            onExportDocx = { triggerExportDocx(saveToDevice = true) },
            onExportPdf = { triggerExportPdf() },
            onCopyGoogleDocs = { triggerCopyGoogleDocs() }
        )

        if (editorState.showRuler) {
            WordRuler(
                orientation = editorState.orientation,
                margins = editorState.margins,
                direction = editorState.direction,
                language = language
            )
        }

        // Main Workspace: Responsive Split / Print Preview / Markdown Source
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp)
        ) {
            val isWide = maxWidth >= 700.dp
            when (editorState.viewMode) {
                EditorViewMode.SOURCE -> {
                    MarkdownSourcePane(
                        markdown = editorState.markdown,
                        placeholder = strings.editorPlaceholder,
                        fontSizeSp = editorState.fontSizeSp,
                        onMarkdownChange = { viewModel.updateMarkdown(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                EditorViewMode.PRINT -> {
                    A4PreviewPane(
                        html = a4Html,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                EditorViewMode.SPLIT -> {
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MarkdownSourcePane(
                                markdown = editorState.markdown,
                                placeholder = strings.editorPlaceholder,
                                fontSizeSp = editorState.fontSizeSp,
                                onMarkdownChange = { viewModel.updateMarkdown(it) },
                                modifier = Modifier
                                    .weight(0.46f)
                                    .fillMaxHeight()
                            )
                            A4PreviewPane(
                                html = a4Html,
                                modifier = Modifier
                                    .weight(0.54f)
                                    .fillMaxHeight()
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MarkdownSourcePane(
                                markdown = editorState.markdown,
                                placeholder = strings.editorPlaceholder,
                                fontSizeSp = editorState.fontSizeSp,
                                onMarkdownChange = { viewModel.updateMarkdown(it) },
                                modifier = Modifier
                                    .weight(0.45f)
                                    .fillMaxWidth()
                            )
                            A4PreviewPane(
                                html = a4Html,
                                modifier = Modifier
                                    .weight(0.55f)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Bottom Sheets
    if (showInsertAiDialog) {
        InsertAiContentDialog(
            strings = strings,
            language = language,
            isFetchingShareLink = editorState.isFetchingShareLink,
            onDismiss = { showInsertAiDialog = false },
            onInsertMarkdown = { md, replace ->
                if (replace) viewModel.replaceDocumentContent(editorState.title, md)
                else viewModel.insertSnippet(md)
            },
            onFetchShareLink = viewModel::fetchAndInsertShareLink
        )
    }

    if (showTableDialog) {
        InsertTableDialog(
            strings = strings,
            language = language,
            onDismiss = { showTableDialog = false },
            onInsertTable = viewModel::insertSnippet
        )
    }

    if (showMathDialog) {
        InsertMathDialog(
            strings = strings,
            language = language,
            onDismiss = { showMathDialog = false },
            onInsertMath = viewModel::insertSnippet
        )
    }

    if (showCodeDialog) {
        InsertCodeDialog(
            strings = strings,
            onDismiss = { showCodeDialog = false },
            onInsertCode = viewModel::insertSnippet
        )
    }

    if (showImageDialog) {
        InsertImageDialog(
            strings = strings,
            language = language,
            onDismiss = { showImageDialog = false },
            onInsertImage = viewModel::insertSnippet
        )
    }

    if (showLinkDialog) {
        InsertLinkDialog(
            strings = strings,
            language = language,
            onDismiss = { showLinkDialog = false },
            onInsertLink = viewModel::insertSnippet
        )
    }

    if (showSymbolDialog) {
        SymbolPaletteDialog(
            strings = strings,
            onDismiss = { showSymbolDialog = false },
            onInsertSymbol = viewModel::insertSnippet
        )
    }

    if (showWatermarkDialog) {
        WatermarkDialog(
            strings = strings,
            currentWatermark = editorState.watermark,
            language = language,
            onDismiss = { showWatermarkDialog = false },
            onApplyWatermark = viewModel::setWatermark
        )
    }

    if (showFindReplaceDialog) {
        FindReplaceDialog(
            strings = strings,
            language = language,
            currentMarkdown = editorState.markdown,
            onDismiss = { showFindReplaceDialog = false },
            onReplaceAll = { find, repl, matchCase ->
                viewModel.findAndReplace(find, repl, matchCase)
            }
        )
    }

    if (showStatsDialog) {
        DocumentStatsDialog(
            strings = strings,
            stats = remember(editorState.markdown) { viewModel.calculateStats() },
            language = language,
            onDismiss = { showStatsDialog = false }
        )
    }

    if (showSavedDocsSheet) {
        SavedDocumentsSheet(
            strings = strings,
            documents = savedDocuments,
            language = language,
            onDismiss = { showSavedDocsSheet = false },
            onOpenDocument = viewModel::loadSavedDocument,
            onDeleteDocument = viewModel::deleteSavedDocument
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(strings.clearDoc) },
            text = { Text(strings.clearConfirm) },
            confirmButton = {
                Button(onClick = {
                    viewModel.clearDocument()
                    showClearConfirm = false
                }) { Text(strings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(strings.cancel) }
            }
        )
    }

    if (editorState.showOutOfCreditsDialog) {
        OutOfCreditsDialog(
            strings = strings,
            language = language,
            onDismiss = viewModel::dismissOutOfCreditsDialog,
            onTopUpCredits = viewModel::topUpCredits
        )
    }
}

@Composable
private fun MarkdownSourcePane(
    markdown: String,
    placeholder: String,
    fontSizeSp: Int,
    onMarkdownChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        OutlinedTextField(
            value = markdown,
            onValueChange = onMarkdownChange,
            placeholder = { Text(placeholder) },
            textStyle = TextStyle(
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = fontSizeSp.coerceIn(11, 18).sp,
                lineHeight = (fontSizeSp.coerceIn(11, 18) * 1.45f).sp
            ),
            modifier = Modifier
                .fillMaxSize()
                .testTag("markdown_source_editor")
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun A4PreviewPane(
    html: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("a4_preview_pane"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = false
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                    }
                },
                update = { webView ->
                    if (webView.tag != html) {
                        webView.tag = html
                        webView.loadDataWithBaseURL("https://midadd.local/", html, "text/html", "utf-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
