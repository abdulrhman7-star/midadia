package com.example.domain.markdown

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipInputStream

data class ImportedDocxResult(
    val title: String,
    val markdown: String
)

/**
 * Pure Kotlin .docx OOXML importer that reads word/document.xml from an OpenXML ZIP stream
 * and reconstructs headings, bold/italic/code formatting, tables, lists, and OMML math into Markdown.
 */
object DocxImporter {

    fun importFromStream(input: InputStream, filename: String): ImportedDocxResult {
        var documentXml: String? = null
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    documentXml = zip.readBytes().toString(Charsets.UTF_8)
                    break
                }
                entry = zip.nextEntry
            }
        }
        val cleanTitle = filename
            .replace(Regex("\\.docx$", RegexOption.IGNORE_CASE), "")
            .replace('-', ' ')
            .replace('_', ' ')
            .trim()
            .ifBlank { "Imported Document" }

        val md = if (documentXml != null) parseDocumentXmlToMarkdown(documentXml!!) else ""
        return ImportedDocxResult(title = cleanTitle, markdown = md)
    }

    private fun parseDocumentXmlToMarkdown(xml: String): String {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xml))

        val blocks = mutableListOf<String>()
        var inParagraph = false
        var inTable = false
        var inTableRow = false
        var inTableCell = false
        var inRun = false
        var inRunPr = false
        var inParagraphPr = false
        var inMath = false

        var pStyle = ""
        var isList = false
        var runBold = false
        var runItalic = false
        var runCode = false
        val paraText = StringBuilder()
        val runText = StringBuilder()
        val mathText = StringBuilder()
        val cellText = StringBuilder()
        val currentRow = mutableListOf<String>()
        val tableRows = mutableListOf<List<String>>()

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val tag = parser.name ?: ""
            when (event) {
                XmlPullParser.START_TAG -> when (tag) {
                    "w:tbl" -> {
                        inTable = true
                        tableRows.clear()
                    }
                    "w:tr" -> if (inTable) {
                        inTableRow = true
                        currentRow.clear()
                    }
                    "w:tc" -> if (inTableRow) {
                        inTableCell = true
                        cellText.clear()
                    }
                    "w:p" -> {
                        inParagraph = true
                        pStyle = ""
                        isList = false
                        paraText.clear()
                    }
                    "w:pPr" -> if (inParagraph) inParagraphPr = true
                    "w:pStyle" -> if (inParagraphPr) {
                        pStyle = parser.getAttributeValue(null, "w:val") ?: ""
                    }
                    "w:numPr" -> if (inParagraphPr) isList = true
                    "m:oMath" -> {
                        inMath = true
                        mathText.clear()
                    }
                    "w:r" -> if (inParagraph) {
                        inRun = true
                        runBold = false
                        runItalic = false
                        runCode = false
                        runText.clear()
                    }
                    "w:rPr" -> if (inRun) inRunPr = true
                    "w:b" -> if (inRunPr) runBold = true
                    "w:i" -> if (inRunPr) runItalic = true
                    "w:rFonts" -> if (inRunPr) {
                        val f = parser.getAttributeValue(null, "w:ascii") ?: ""
                        if (f.contains("Consolas", ignoreCase = true) || f.contains("Mono", ignoreCase = true)) {
                            runCode = true
                        }
                    }
                    "w:t", "m:t" -> {
                        val text = parser.nextText()
                        if (inMath) mathText.append(text)
                        else if (inRun) runText.append(text)
                    }
                }

                XmlPullParser.END_TAG -> when (tag) {
                    "w:rPr" -> inRunPr = false
                    "w:r" -> {
                        if (inRun) {
                            val raw = runText.toString()
                            if (raw.isNotEmpty()) {
                                val formatted = when {
                                    runCode -> "`$raw`"
                                    runBold && runItalic -> "***$raw***"
                                    runBold -> "**$raw**"
                                    runItalic -> "*$raw*"
                                    else -> raw
                                }
                                paraText.append(formatted)
                            }
                            inRun = false
                        }
                    }
                    "m:oMath" -> {
                        if (inMath) {
                            val m = mathText.toString().trim()
                            if (m.isNotEmpty()) paraText.append("\$$m\$")
                            inMath = false
                        }
                    }
                    "w:pPr" -> inParagraphPr = false
                    "w:p" -> {
                        if (inParagraph) {
                            val line = paraText.toString().trim()
                            if (inTableCell) {
                                if (line.isNotEmpty()) {
                                    if (cellText.isNotEmpty()) cellText.append(" ")
                                    cellText.append(line.replace("|", "\\|"))
                                }
                            } else if (line.isNotEmpty()) {
                                val prefix = when {
                                    pStyle.equals("Heading1", ignoreCase = true) || pStyle == "1" -> "# "
                                    pStyle.equals("Heading2", ignoreCase = true) || pStyle == "2" -> "## "
                                    pStyle.equals("Heading3", ignoreCase = true) || pStyle == "3" -> "### "
                                    pStyle.equals("Heading4", ignoreCase = true) || pStyle == "4" -> "#### "
                                    isList -> "- "
                                    else -> ""
                                }
                                blocks.add("$prefix$line")
                            }
                            inParagraph = false
                        }
                    }
                    "w:tc" -> {
                        if (inTableCell) {
                            currentRow.add(cellText.toString().trim())
                            inTableCell = false
                        }
                    }
                    "w:tr" -> {
                        if (inTableRow) {
                            if (currentRow.isNotEmpty()) tableRows.add(currentRow.toList())
                            inTableRow = false
                        }
                    }
                    "w:tbl" -> {
                        if (inTable && tableRows.isNotEmpty()) {
                            val cols = tableRows.maxOf { it.size }
                            val sb = StringBuilder()
                            val head = tableRows.first()
                            sb.append("|")
                            for (c in 0 until cols) sb.append(" ${head.getOrNull(c) ?: "Col ${c + 1}"} |")
                            sb.append("\n|")
                            for (c in 0 until cols) sb.append(" :--- |")
                            for (r in 1 until tableRows.size) {
                                sb.append("\n|")
                                for (c in 0 until cols) sb.append(" ${tableRows[r].getOrNull(c) ?: ""} |")
                            }
                            blocks.add(sb.toString())
                        }
                        inTable = false
                    }
                }
            }
            event = parser.next()
        }
        return blocks.joinToString("\n\n").trim()
    }
}
