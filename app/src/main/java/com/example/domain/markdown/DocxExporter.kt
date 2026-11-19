package com.example.domain.markdown

import com.example.data.model.PageMargins
import com.example.data.model.PageOrientation
import com.example.data.model.TextDirectionMode
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class DocxExportOptions(
    val orientation: PageOrientation = PageOrientation.PORTRAIT,
    val margins: PageMargins = PageMargins.NORMAL,
    val fontFamily: String = "Cairo",
    val fontSizePt: Int = 11,
    val direction: TextDirectionMode = TextDirectionMode.RTL,
    val watermark: String = ""
)

/**
 * Generates authentic OpenXML (.docx) bytes containing native Word paragraphs,
 * styled tables, code blocks, RTL bidirectional flags, and editable OMML math equations.
 */
object DocxExporter {

    fun markdownToDocxBytes(markdown: String, options: DocxExportOptions = DocxExportOptions()): ByteArray {
        val documentXml = buildDocumentXml(markdown, options)
        val stylesXml = buildStylesXml(options)
        val numberingXml = buildNumberingXml()
        val contentTypesXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
              <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
              <Default Extension="xml" ContentType="application/xml"/>
              <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
              <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
              <Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>
            </Types>
        """.trimIndent()

        val rootRelsXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
              <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
            </Relationships>
        """.trimIndent()

        val docRelsXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
              <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
              <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering" Target="numbering.xml"/>
            </Relationships>
        """.trimIndent()

        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            fun addEntry(name: String, content: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            addEntry("[Content_Types].xml", contentTypesXml)
            addEntry("_rels/.rels", rootRelsXml)
            addEntry("word/_rels/document.xml.rels", docRelsXml)
            addEntry("word/styles.xml", stylesXml)
            addEntry("word/numbering.xml", numberingXml)
            addEntry("word/document.xml", documentXml)
        }
        return out.toByteArray()
    }

    private fun buildDocumentXml(markdown: String, options: DocxExportOptions): String {
        val bodyElements = mutableListOf<String>()
        val isRtl = options.direction == TextDirectionMode.RTL
        val bidiTag = if (isRtl) "<w:bidi/>" else ""

        if (options.watermark.isNotBlank()) {
            bodyElements.add(
                "<w:p><w:pPr><w:jc w:val=\"center\"/><w:spacing w:after=\"120\"/></w:pPr>" +
                    "<w:r><w:rPr><w:color w:val=\"94A3B8\"/><w:sz w:val=\"20\"/><w:b/></w:rPr>" +
                    "<w:t xml:space=\"preserve\">[${LatexToOmml.escapeXml(options.watermark.uppercase())}]</w:t></w:r></w:p>"
            )
        }

        val lines = markdown.split("\n")
        var inCodeBlock = false
        val codeLines = mutableListOf<String>()
        var inTable = false
        val tableRows = mutableListOf<List<String>>()

        fun flushCodeBlock() {
            if (inCodeBlock) {
                val codeText = LatexToOmml.escapeXml(codeLines.joinToString("\n"))
                bodyElements.add(
                    "<w:p><w:pPr>" +
                        "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"F1F5F9\"/>" +
                        "<w:pBdr>" +
                        "<w:left w:val=\"single\" w:sz=\"12\" w:space=\"6\" w:color=\"64748B\"/>" +
                        "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"4\" w:color=\"E2E8F0\"/>" +
                        "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"4\" w:color=\"E2E8F0\"/>" +
                        "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"4\" w:color=\"E2E8F0\"/>" +
                        "</w:pBdr>" +
                        "<w:spacing w:before=\"160\" w:after=\"160\" w:line=\"260\" w:lineRule=\"auto\"/>" +
                        "<w:ind w:left=\"240\" w:right=\"240\"/>" +
                        "</w:pPr>" +
                        "<w:r><w:rPr><w:rFonts w:ascii=\"Consolas\" w:hAnsi=\"Consolas\"/><w:sz w:val=\"19\"/><w:color w:val=\"1E293B\"/></w:rPr>" +
                        "<w:t xml:space=\"preserve\">$codeText</w:t></w:r></w:p>"
                )
                codeLines.clear()
                inCodeBlock = false
            }
        }

        fun flushTable() {
            if (inTable && tableRows.isNotEmpty()) {
                val sb = StringBuilder()
                val tblBidi = if (isRtl) "<w:bidiVisual/>" else ""
                sb.append(
                    "<w:tbl><w:tblPr>" +
                        "<w:tblW w:w=\"5000\" w:type=\"pct\"/>" +
                        tblBidi +
                        "<w:tblBorders>" +
                        "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>" +
                        "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>" +
                        "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>" +
                        "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>" +
                        "<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>" +
                        "<w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>" +
                        "</w:tblBorders></w:tblPr>"
                )
                tableRows.forEachIndexed { rowIdx, cells ->
                    val isHeader = rowIdx == 0
                    val trPr = if (isHeader) "<w:trPr><w:tblHeader/></w:trPr>" else ""
                    sb.append("<w:tr>$trPr")
                    cells.forEach { cell ->
                        val shading = when {
                            isHeader -> "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"F1F5F9\"/>"
                            rowIdx % 2 == 0 -> "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"F8FAFC\"/>"
                            else -> ""
                        }
                        val jc = if (isHeader) "center" else if (isRtl) "right" else "left"
                        sb.append(
                            "<w:tc><w:tcPr>$shading" +
                                "<w:tcMar><w:top w:w=\"100\" w:type=\"dxa\"/><w:bottom w:w=\"100\" w:type=\"dxa\"/>" +
                                "<w:left w:w=\"140\" w:type=\"dxa\"/><w:right w:w=\"140\" w:type=\"dxa\"/></w:tcMar>" +
                                "</w:tcPr>" +
                                "<w:p><w:pPr>$bidiTag<w:jc w:val=\"$jc\"/><w:spacing w:before=\"60\" w:after=\"60\"/></w:pPr>" +
                                parseInlineOoxml(cell.trim(), boldDefault = isHeader) +
                                "</w:p></w:tc>"
                        )
                    }
                    sb.append("</w:tr>")
                }
                sb.append("</w:tbl><w:p><w:pPr><w:spacing w:after=\"120\"/></w:pPr></w:p>")
                bodyElements.add(sb.toString())
                tableRows.clear()
                inTable = false
            }
        }

        fun isTableRow(line: String): Boolean {
            val t = line.trim()
            return t.startsWith("|") && t.endsWith("|") && t.length > 1
        }

        fun isTableSeparator(line: String): Boolean {
            val t = line.trim()
            if (!t.startsWith("|") || !t.endsWith("|")) return false
            val inner = t.substring(1, t.length - 1)
            return Regex("^(\\s*:?-+:?\\s*\\|)*\\s*:?-+:?\\s*$").matches(inner)
        }

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                flushTable()
                if (!inCodeBlock) {
                    inCodeBlock = true
                    codeLines.clear()
                } else {
                    flushCodeBlock()
                }
                i++
                continue
            }

            if (inCodeBlock) {
                codeLines.add(line)
                i++
                continue
            }

            if (isTableRow(line)) {
                if (!isTableSeparator(line)) {
                    inTable = true
                    tableRows.add(line.trim().drop(1).dropLast(1).split("|").map { it.trim() })
                }
                i++
                continue
            } else {
                flushTable()
            }

            // Single-line $$...$$ display math -> native OMML
            if (trimmed.startsWith("$$") && trimmed.endsWith("$$") && trimmed.length > 4) {
                val formula = trimmed.substring(2, trimmed.length - 2).trim()
                val oMath = LatexToOmml.toOmmlParagraphElement(formula)
                bodyElements.add(
                    "<w:p><w:pPr><w:jc w:val=\"center\"/><w:spacing w:before=\"160\" w:after=\"160\"/></w:pPr>$oMath</w:p>"
                )
                i++
                continue
            }

            // Multi-line $$ or \[
            if (trimmed == "$$" || trimmed == "\\[") {
                val mathLines = mutableListOf<String>()
                i++
                while (i < lines.size && lines[i].trim() != "$$" && lines[i].trim() != "\\]") {
                    mathLines.add(lines[i])
                    i++
                }
                val formula = mathLines.joinToString(" ").trim()
                val oMath = LatexToOmml.toOmmlParagraphElement(formula)
                bodyElements.add(
                    "<w:p><w:pPr><w:jc w:val=\"center\"/><w:spacing w:before=\"160\" w:after=\"160\"/></w:pPr>$oMath</w:p>"
                )
                i++
                continue
            }

            if (trimmed.isEmpty()) {
                i++
                continue
            }

            if (trimmed == "<!-- pagebreak -->") {
                bodyElements.add("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>")
                i++
                continue
            }

            if (Regex("^(---+|\\*\\*\\*+|___+)$").matches(trimmed)) {
                bodyElements.add(
                    "<w:p><w:pPr><w:pBdr><w:bottom w:val=\"single\" w:sz=\"6\" w:space=\"1\" w:color=\"CBD5E1\"/></w:pBdr>" +
                        "<w:spacing w:before=\"120\" w:after=\"120\"/></w:pPr></w:p>"
                )
                i++
                continue
            }

            if (trimmed.startsWith(">")) {
                val qText = trimmed.replaceFirst(Regex("^>\\s?"), "")
                bodyElements.add(
                    "<w:p><w:pPr>$bidiTag" +
                        "<w:pBdr><w:left w:val=\"single\" w:sz=\"24\" w:space=\"8\" w:color=\"6366F1\"/></w:pBdr>" +
                        "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"F8FAFC\"/>" +
                        "<w:ind w:left=\"360\"/>" +
                        "<w:spacing w:before=\"120\" w:after=\"120\"/>" +
                        "</w:pPr>${parseInlineOoxml(qText)}</w:p>"
                )
                i++
                continue
            }

            when {
                trimmed.startsWith("#### ") -> {
                    bodyElements.add(
                        "<w:p><w:pPr><w:pStyle w:val=\"Heading4\"/>$bidiTag<w:spacing w:before=\"180\" w:after=\"80\"/></w:pPr>" +
                            "${parseInlineOoxml(trimmed.drop(5))}</w:p>"
                    )
                }
                trimmed.startsWith("### ") -> {
                    bodyElements.add(
                        "<w:p><w:pPr><w:pStyle w:val=\"Heading3\"/>$bidiTag<w:spacing w:before=\"220\" w:after=\"100\"/></w:pPr>" +
                            "${parseInlineOoxml(trimmed.drop(4))}</w:p>"
                    )
                }
                trimmed.startsWith("## ") -> {
                    bodyElements.add(
                        "<w:p><w:pPr><w:pStyle w:val=\"Heading2\"/>$bidiTag<w:spacing w:before=\"280\" w:after=\"140\"/></w:pPr>" +
                            "${parseInlineOoxml(trimmed.drop(3))}</w:p>"
                    )
                }
                trimmed.startsWith("# ") -> {
                    bodyElements.add(
                        "<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/>$bidiTag<w:spacing w:before=\"360\" w:after=\"180\"/></w:pPr>" +
                            "${parseInlineOoxml(trimmed.drop(2))}</w:p>"
                    )
                }
                Regex("^[-*]\\s+\\[[ xX]]\\s+").containsMatchIn(trimmed) -> {
                    val checked = Regex("^[-*]\\s+\\[([xX])]").containsMatchIn(trimmed)
                    val clean = trimmed.replaceFirst(Regex("^[-*]\\s+\\[[ xX]]\\s+"), "")
                    val prefix = if (checked) "☑ " else "☐ "
                    bodyElements.add(
                        "<w:p><w:pPr>$bidiTag<w:ind w:left=\"360\"/><w:spacing w:before=\"60\" w:after=\"60\"/></w:pPr>" +
                            "<w:r><w:t xml:space=\"preserve\">$prefix</w:t></w:r>${parseInlineOoxml(clean)}</w:p>"
                    )
                }
                Regex("^[-*]\\s+").containsMatchIn(trimmed) -> {
                    val clean = trimmed.replaceFirst(Regex("^[-*]\\s+"), "")
                    bodyElements.add(
                        "<w:p><w:pPr>$bidiTag<w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr>" +
                            "<w:spacing w:before=\"60\" w:after=\"60\"/></w:pPr>${parseInlineOoxml(clean)}</w:p>"
                    )
                }
                Regex("^\\d+\\.\\s+").containsMatchIn(trimmed) -> {
                    val numPrefix = Regex("^(\\d+\\.\\s+)").find(trimmed)?.groupValues?.get(1) ?: "1. "
                    val clean = trimmed.replaceFirst(Regex("^\\d+\\.\\s+"), "")
                    bodyElements.add(
                        "<w:p><w:pPr>$bidiTag<w:ind w:left=\"360\"/><w:spacing w:before=\"60\" w:after=\"60\"/></w:pPr>" +
                            "<w:r><w:rPr><w:b/></w:rPr><w:t xml:space=\"preserve\">${LatexToOmml.escapeXml(numPrefix)}</w:t></w:r>" +
                            "${parseInlineOoxml(clean)}</w:p>"
                    )
                }
                else -> {
                    bodyElements.add(
                        "<w:p><w:pPr>$bidiTag<w:spacing w:before=\"100\" w:after=\"100\" w:line=\"276\" w:lineRule=\"auto\"/></w:pPr>" +
                            "${parseInlineOoxml(line)}</w:p>"
                    )
                }
            }
            i++
        }

        flushCodeBlock()
        flushTable()

        val marginVal = when (options.margins) {
            PageMargins.NARROW -> 720
            PageMargins.WIDE -> 2160
            PageMargins.NORMAL -> 1440
        }
        val isLandscape = options.orientation == PageOrientation.LANDSCAPE
        val pgSz = if (isLandscape) {
            "<w:pgSz w:w=\"16838\" w:h=\"11906\" w:orient=\"landscape\"/>"
        } else {
            "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
        }

        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:document
              xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
              xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
              xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
              <w:body>
                ${bodyElements.joinToString("\n")}
                <w:sectPr>
                  $pgSz
                  <w:pgMar w:top="$marginVal" w:right="$marginVal" w:bottom="$marginVal" w:left="$marginVal" w:header="720" w:footer="720" w:gutter="0"/>
                </w:sectPr>
              </w:body>
            </w:document>
        """.trimIndent()
    }

    private fun parseInlineOoxml(text: String, boldDefault: Boolean = false): String {
        if (text.isEmpty()) return ""
        val tokenRegex = Regex("(\\$[^$\\n]+\\$|\\*\\*[^*]+\\*\\*|\\*[^*]+\\*|`[^`]+`)")
        val sb = StringBuilder()
        var lastIndex = 0
        for (match in tokenRegex.findAll(text)) {
            if (match.range.first > lastIndex) {
                val plain = text.substring(lastIndex, match.range.first)
                sb.append(textRunXml(plain, bold = boldDefault))
            }
            val token = match.value
            when {
                token.startsWith("$") && token.endsWith("$") && token.length > 2 -> {
                    val formula = token.substring(1, token.length - 1)
                    sb.append(LatexToOmml.toOmmlParagraphElement(formula))
                }
                token.startsWith("**") && token.endsWith("**") && token.length > 4 -> {
                    sb.append(textRunXml(token.substring(2, token.length - 2), bold = true))
                }
                token.startsWith("*") && token.endsWith("*") && token.length > 2 -> {
                    sb.append(textRunXml(token.substring(1, token.length - 1), bold = boldDefault, italic = true))
                }
                token.startsWith("`") && token.endsWith("`") && token.length > 2 -> {
                    sb.append(
                        "<w:r><w:rPr><w:rFonts w:ascii=\"Consolas\" w:hAnsi=\"Consolas\"/><w:sz w:val=\"19\"/>" +
                            "<w:color w:val=\"DC2626\"/><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"F1F5F9\"/></w:rPr>" +
                            "<w:t xml:space=\"preserve\">${LatexToOmml.escapeXml(token.substring(1, token.length - 1))}</w:t></w:r>"
                    )
                }
            }
            lastIndex = match.range.last + 1
        }
        if (lastIndex < text.length) {
            sb.append(textRunXml(text.substring(lastIndex), bold = boldDefault))
        }
        return sb.toString()
    }

    private fun textRunXml(text: String, bold: Boolean = false, italic: Boolean = false): String {
        val rPr = buildString {
            if (bold) append("<w:b/>")
            if (italic) append("<w:i/>")
        }
        val rPrTag = if (rPr.isNotEmpty()) "<w:rPr>$rPr</w:rPr>" else ""
        return "<w:r>$rPrTag<w:t xml:space=\"preserve\">${LatexToOmml.escapeXml(text)}</w:t></w:r>"
    }

    private fun buildStylesXml(options: DocxExportOptions): String {
        val sz = options.fontSizePt * 2
        val font = LatexToOmml.escapeXml(options.fontFamily)
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
              <w:docDefaults>
                <w:rPrDefault>
                  <w:rPr>
                    <w:rFonts w:ascii="$font" w:hAnsi="$font" w:cs="$font"/>
                    <w:sz w:val="$sz"/>
                    <w:szCs w:val="$sz"/>
                    <w:color w:val="1E293B"/>
                  </w:rPr>
                </w:rPrDefault>
                <w:pPrDefault>
                  <w:pPr><w:spacing w:line="276" w:lineRule="auto"/></w:pPr>
                </w:pPrDefault>
              </w:docDefaults>
              <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
                <w:name w:val="Normal"/>
              </w:style>
              <w:style w:type="paragraph" w:styleId="Heading1">
                <w:name w:val="heading 1"/>
                <w:basedOn w:val="Normal"/>
                <w:rPr><w:b/><w:sz w:val="40"/><w:szCs w:val="40"/><w:color w:val="0F172A"/></w:rPr>
              </w:style>
              <w:style w:type="paragraph" w:styleId="Heading2">
                <w:name w:val="heading 2"/>
                <w:basedOn w:val="Normal"/>
                <w:rPr><w:b/><w:sz w:val="32"/><w:szCs w:val="32"/><w:color w:val="0F172A"/></w:rPr>
              </w:style>
              <w:style w:type="paragraph" w:styleId="Heading3">
                <w:name w:val="heading 3"/>
                <w:basedOn w:val="Normal"/>
                <w:rPr><w:b/><w:sz w:val="26"/><w:szCs w:val="26"/><w:color w:val="1E293B"/></w:rPr>
              </w:style>
              <w:style w:type="paragraph" w:styleId="Heading4">
                <w:name w:val="heading 4"/>
                <w:basedOn w:val="Normal"/>
                <w:rPr><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/><w:color w:val="334155"/></w:rPr>
              </w:style>
            </w:styles>
        """.trimIndent()
    }

    private fun buildNumberingXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
          <w:abstractNum w:abstractNumId="0">
            <w:lvl w:ilvl="0">
              <w:start w:val="1"/>
              <w:numFmt w:val="bullet"/>
              <w:lvlText w:val="•"/>
              <w:lvlJc w:val="left"/>
              <w:pPr><w:ind w:left="720" w:hanging="360"/></w:pPr>
            </w:lvl>
          </w:abstractNum>
          <w:num w:numId="1">
            <w:abstractNumId w:val="0"/>
          </w:num>
        </w:numbering>
    """.trimIndent()
}
