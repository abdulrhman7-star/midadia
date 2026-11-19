package com.example.domain.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import com.example.data.model.PageOrientation
import java.io.File

object AndroidExportHelper {

    fun sanitizeFileName(title: String): String {
        val cleaned = title
            .replace(Regex("[^\\p{L}\\p{N}\\s_-]"), "")
            .trim()
            .replace(Regex("\\s+"), "_")
            .ifBlank { "midadd_document" }
        return cleaned.take(48)
    }

    fun writeDocxToUri(context: Context, uri: Uri, bytes: ByteArray): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(bytes)
                out.flush()
            } ?: error("Cannot open output stream")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareDocxFile(context: Context, title: String, bytes: ByteArray): Result<Unit> {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "${sanitizeFileName(title)}.docx")
            file.writeBytes(bytes)
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "$title.docx")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "$title.docx").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun copyForGoogleDocs(context: Context, plainMarkdown: String, html: String): Result<Unit> {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newHtmlText("midadd Document", plainMarkdown, html)
            clipboard.setPrimaryClip(clip)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun copyPlainText(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun printHtmlToPdf(
        context: Context,
        title: String,
        html: String,
        orientation: PageOrientation,
        onJobStarted: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                onError("Print service unavailable on this device")
                return
            }
            val webView = WebView(context)
            webView.settings.javaScriptEnabled = false
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

                override fun onPageFinished(view: WebView?, url: String?) {
                    val safeName = sanitizeFileName(title)
                    val adapter = view?.createPrintDocumentAdapter(safeName) ?: return
                    val mediaSize = if (orientation == PageOrientation.LANDSCAPE) {
                        PrintAttributes.MediaSize.ISO_A4.asLandscape()
                    } else {
                        PrintAttributes.MediaSize.ISO_A4.asPortrait()
                    }
                    val attrs = PrintAttributes.Builder()
                        .setMediaSize(mediaSize)
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                    printManager.print(safeName, adapter, attrs)
                    onJobStarted()
                }
            }
            webView.loadDataWithBaseURL("https://midadd.local/", html, "text/html", "utf-8", null)
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "PDF export failed")
        }
    }
}
