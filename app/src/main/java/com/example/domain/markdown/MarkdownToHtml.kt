package com.example.domain.markdown

import com.example.data.model.MarkdownPreset
import com.example.data.model.PageMargins
import com.example.data.model.PageOrientation
import com.example.data.model.TextDirectionMode

object MarkdownToHtml {

    val SAMPLE_MARKDOWN_PRESETS: List<MarkdownPreset> = listOf(
        MarkdownPreset(
            id = "quantum",
            titleEn = "Quantum Entanglement & Superposition Report",
            titleAr = "ورقة بحثية: التشابك والتراكب الكمي",
            markdown = """
# ورقة بحثية: دراسة التراكب والتشابك الكمي

> **ملخص البحث:** يهدف هذا المستند إلى تحليل ظاهرة التشابك الكمي (Quantum Entanglement) وحساب متجهات الحالات الحسابية بأسلوب أكاديمي احترافي.

---

## 1. المقدمة والخلفية النظرية

تُعد الميكانيكا الكمية إحدى أهم الثورات العلمية في الفيزياء الحديثة. تعتمد صيغة حالة الكيوبت (Qubit) العامة على حالة التراكب:

$$\psi = \alpha |0\rangle + \beta |1\rangle$$

حيث تحقق الأعداد المركبة $\alpha$ و $\beta$ الشروط التالية:

$$|\alpha|^2 + |\beta|^2 = 1$$

---

## 2. مصفوفة الكثافة وحالة بيل (Bell State)

تتميز حالات بيل المشابكة بصيغتها الرياضية المعيارية:

$$|\Phi^+\rangle = \frac{1}{\sqrt{2}} \left( |00\rangle + |11\rangle \right)$$

### جدول المقارنة للحالات الكمية:

| نوع الحالة | الصيغة الرياضية | درجة التشابك | التطبيق العملي |
| :--- | :---: | :---: | ---: |
| Qubit Single | $\alpha|0\rangle + \beta|1\rangle$ | $0.0$ | التخزين الحسابي |
| Bell State | $\frac{1}{\sqrt{2}}(|00\rangle + |11\rangle)$ | $1.0$ | التشفير الكمي |
| GHZ State | $\frac{1}{\sqrt{2}}(|000\rangle + |111\rangle)$ | $1.0$ | الاتصالات الجماعية |

---

## 3. خوارزمية المحاكاة البرمجية (Python)

```python
import numpy as np

def generate_bell_state():
    # Bell state creation matrix
    qubit_00 = np.array([1, 0, 0, 0])
    qubit_11 = np.array([0, 0, 0, 1])
    bell_phi_plus = (qubit_00 + qubit_11) / np.sqrt(2)
    return bell_phi_plus

print("Bell State Vector:", generate_bell_state())
```

- [x] إعداد صيغة المعادلات الرياضيات OMML
- [x] التحقق من الجداول وتنسيق العناوين
- [ ] المراجعة النهائية والتصدير لملف Word
            """.trimIndent()
        ),
        MarkdownPreset(
            id = "saudi_vat",
            titleEn = "Saudi VAT & Financial Compliance Report",
            titleAr = "تقرير الإقرار الضريبي للضريبة المضافة (ZATCA)",
            markdown = """
# تقرير الإقرار الضريبي وضريبة القيمة المضافة (ZATCA)

**اسم الشركة:** شركة مداد التقنية للحلول الرقمية  
**الرقم الضريبي (VAT ID):** `310123456700003`  
**الفصل الضريبي:** الربع الأول 2026  

---

## 1. ملخص الإقرارات والمبيعات

يوضح الجدول التالي تفاصيل المبيعات الخاضعة للنسبة الأساسية (15%) والمبيعات المعفاة والصافية:

| بند الإقرار | المبلغ الخاضع (SAR) | ضريبة القيمة المضافة 15% | الصافي الاجمالي |
| :--- | :---: | :---: | ---: |
| المبيعات الخاضعة للنسبة الأساسية (15%) | 120,000.00 | 18,000.00 | 138,000.00 |
| المشتريات الخاضعة للنسبة الأساسية (15%) | 40,000.00 | 6,000.00 | 46,000.00 |
| **صافي الضريبة المستحقة للدفع** | **80,000.00** | **12,000.00** | **92,000.00** |

---

## 2. المعادلة الحسابية للضريبة الصافية

$$\text{Net VAT Payable} = \sum \text{Output VAT} - \sum \text{Input VAT}$$

$$\text{Net VAT} = 18,000.00 - 6,000.00 = 12,000.00 \text{ SAR}$$

> [!NOTE]
> يجب سداد الإقرار الضريبي قبل نهاية الشهر التالي للفصل الضريبي لضمان الامتثال لتعليمات هيئة الزكاة والضريبة والجمارك.
            """.trimIndent()
        ),
        MarkdownPreset(
            id = "neural_network",
            titleEn = "Neural Network Architecture & AI Specification",
            titleAr = "مواصفات شبكة التعلم العميق والذكاء الاصطناعي",
            markdown = """
# مواصفات شبكة التعلم العميق Transformer Model Architecture

## 1. بنية النموذج والمكونات

تعتمد البنية على آلية الانتباه الذاتي المترابط (Multi-Head Self-Attention):

$$\text{Attention}(Q, K, V) = \text{softmax}\left(\frac{QK^T}{\sqrt{d_k}}\right)V$$

---

## 2. معلمات الطبقات والتأخير

| الطبقة (Layer) | الأبعاد (Dimensions) | زمن المعالجة (ms) |
| :--- | :---: | ---: |
| Embedding Layer | $512 \times 768$ | 1.2 ms |
| Multi-Head Attention | $8 \text{ heads}, d_k=64$ | 4.5 ms |
| Feed Forward Layer | $2048 \text{ hidden units}$ | 3.1 ms |

---

## 3. كود تنفيذ دالة الخسارة (Loss Function)

```python
import torch
import torch.nn as nn

class CrossEntropyLossWithSmoothing(nn.Module):
    def __init__(self, label_smoothing=0.1):
        super().__init__()
        self.confidence = 1.0 - label_smoothing

    def forward(self, x, target):
        logprobs = torch.log_softmax(x, dim=-1)
        nll_loss = -logprobs.gather(dim=-1, index=target.unsqueeze(-1)).squeeze(-1)
        return nll_loss.mean()
```
            """.trimIndent()
        )
    )

    fun escapeHtml(unsafe: String): String = unsafe
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#039;")

    fun markdownToHtmlFragment(markdown: String, direction: TextDirectionMode = TextDirectionMode.LTR): String {
        if (markdown.isBlank()) return ""
        val dirStr = direction.wireValue
        val alignClass = if (direction == TextDirectionMode.RTL) "text-right" else "text-left"

        val lines = markdown.split("\n")
        val htmlParts = mutableListOf<String>()

        var inCodeBlock = false
        var codeLang = ""
        val codeLines = mutableListOf<String>()

        var inTable = false
        val tableRows = mutableListOf<List<String>>()
        var tableAlignments = listOf<String>()

        var inBlockquote = false
        val quoteLines = mutableListOf<String>()

        var inList = false
        var listType = "ul"

        fun flushList() {
            if (inList) {
                htmlParts.add("</$listType>")
                inList = false
            }
        }

        fun flushBlockquote() {
            if (inBlockquote) {
                val joined = quoteLines.joinToString("\n")
                val isWarning = joined.contains("[!WARNING]", ignoreCase = true)
                val isNote = joined.contains("[!NOTE]", ignoreCase = true)
                val cleaned = joined
                    .replace("[!WARNING]", "")
                    .replace("[!NOTE]", "")
                    .trim()
                    .replace("\n", "<br/>")
                val cls = when {
                    isWarning -> "callout callout-warning"
                    isNote -> "callout callout-note"
                    else -> "blockquote"
                }
                htmlParts.add("<blockquote class=\"$cls $alignClass\" dir=\"$dirStr\">${parseInlineMarkdown(cleaned)}</blockquote>")
                quoteLines.clear()
                inBlockquote = false
            }
        }

        fun flushTable() {
            if (inTable && tableRows.isNotEmpty()) {
                val sb = StringBuilder()
                sb.append("<div class=\"table-wrap\" dir=\"$dirStr\"><table class=\"editor-table\">")
                val headers = tableRows.first()
                sb.append("<thead><tr>")
                headers.forEachIndexed { idx, h ->
                    val align = tableAlignments.getOrNull(idx) ?: if (direction == TextDirectionMode.RTL) "right" else "left"
                    sb.append("<th style=\"text-align:$align\">${parseInlineMarkdown(h)}</th>")
                }
                sb.append("</tr></thead><tbody>")
                for (r in 1 until tableRows.size) {
                    val row = tableRows[r]
                    val cls = if (r % 2 == 0) "even" else "odd"
                    sb.append("<tr class=\"$cls\">")
                    row.forEachIndexed { idx, cell ->
                        val align = tableAlignments.getOrNull(idx) ?: if (direction == TextDirectionMode.RTL) "right" else "left"
                        sb.append("<td style=\"text-align:$align\">${parseInlineMarkdown(cell)}</td>")
                    }
                    sb.append("</tr>")
                }
                sb.append("</tbody></table></div>")
                htmlParts.add(sb.toString())
                tableRows.clear()
                tableAlignments = emptyList()
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

            // 1. Code fence
            if (trimmed.startsWith("```")) {
                flushList()
                flushBlockquote()
                flushTable()
                if (!inCodeBlock) {
                    inCodeBlock = true
                    codeLang = trimmed.drop(3).trim().ifEmpty { "code" }
                    codeLines.clear()
                } else {
                    inCodeBlock = false
                    val codeText = escapeHtml(codeLines.joinToString("\n"))
                    htmlParts.add(
                        "<div class=\"code-card\" dir=\"ltr\">" +
                            "<div class=\"code-header\"><span>${escapeHtml(codeLang)}</span><span>Code</span></div>" +
                            "<pre class=\"code-pre\"><code>$codeText</code></pre>" +
                            "</div>"
                    )
                }
                i++
                continue
            }

            if (inCodeBlock) {
                codeLines.add(line)
                i++
                continue
            }

            // 2. Display math $$...$$ single line
            if (trimmed.startsWith("$$") && trimmed.endsWith("$$") && trimmed.length > 4) {
                flushList()
                flushBlockquote()
                flushTable()
                val formula = trimmed.substring(2, trimmed.length - 2).trim()
                val rendered = LatexToOmml.renderLatexToPreviewHtml(formula, displayMode = true)
                htmlParts.add("<div class=\"math-block\">$rendered</div>")
                i++
                continue
            }

            // Multiline $$ or \[
            if (trimmed == "$$" || trimmed == "\\[") {
                flushList()
                flushBlockquote()
                flushTable()
                val mathLines = mutableListOf<String>()
                i++
                while (i < lines.size && lines[i].trim() != "$$" && lines[i].trim() != "\\]") {
                    mathLines.add(lines[i])
                    i++
                }
                val formula = mathLines.joinToString(" ").trim()
                val rendered = LatexToOmml.renderLatexToPreviewHtml(formula, displayMode = true)
                htmlParts.add("<div class=\"math-block\">$rendered</div>")
                i++
                continue
            }

            // 3. Table
            if (isTableRow(line)) {
                flushList()
                flushBlockquote()
                if (isTableSeparator(line)) {
                    val cols = line.trim().drop(1).dropLast(1).split("|")
                    tableAlignments = cols.map { col ->
                        val t = col.trim()
                        when {
                            t.startsWith(":") && t.endsWith(":") -> "center"
                            t.endsWith(":") -> "right"
                            else -> "left"
                        }
                    }
                } else {
                    inTable = true
                    val cells = line.trim().drop(1).dropLast(1).split("|").map { it.trim() }
                    tableRows.add(cells)
                }
                i++
                continue
            } else {
                flushTable()
            }

            // 4. Blockquote
            if (trimmed.startsWith(">")) {
                flushList()
                inBlockquote = true
                quoteLines.add(trimmed.replaceFirst(Regex("^>\\s?"), ""))
                i++
                continue
            } else {
                flushBlockquote()
            }

            // 5. Empty line
            if (trimmed.isEmpty()) {
                flushList()
                i++
                continue
            }

            // Page break comment
            if (trimmed == "<!-- pagebreak -->") {
                flushList()
                htmlParts.add("<div class=\"page-break\">--- Page Break ---</div>")
                i++
                continue
            }

            // 6. Horizontal rule
            if (Regex("^(---+|\\*\\*\\*+|___+)$").matches(trimmed)) {
                flushList()
                htmlParts.add("<hr/>")
                i++
                continue
            }

            // 7. Headings
            if (trimmed.startsWith("#### ")) {
                flushList()
                htmlParts.add("<h4 class=\"$alignClass\" dir=\"$dirStr\">${parseInlineMarkdown(trimmed.drop(5))}</h4>")
                i++
                continue
            }
            if (trimmed.startsWith("### ")) {
                flushList()
                htmlParts.add("<h3 class=\"$alignClass\" dir=\"$dirStr\">${parseInlineMarkdown(trimmed.drop(4))}</h3>")
                i++
                continue
            }
            if (trimmed.startsWith("## ")) {
                flushList()
                htmlParts.add("<h2 class=\"$alignClass\" dir=\"$dirStr\">${parseInlineMarkdown(trimmed.drop(3))}</h2>")
                i++
                continue
            }
            if (trimmed.startsWith("# ")) {
                flushList()
                htmlParts.add("<h1 class=\"$alignClass\" dir=\"$dirStr\">${parseInlineMarkdown(trimmed.drop(2))}</h1>")
                i++
                continue
            }

            // 8. Task list
            val taskMatch = Regex("^\\s*[-*]\\s+\\[([ xX])]\\s+(.*)$").find(line)
            if (taskMatch != null) {
                if (!inList || listType != "ul") {
                    flushList()
                    inList = true
                    listType = "ul"
                    htmlParts.add("<ul class=\"task-list $alignClass\" dir=\"$dirStr\">")
                }
                val checked = taskMatch.groupValues[1].equals("x", ignoreCase = true)
                val content = taskMatch.groupValues[2]
                val checkbox = if (checked) "&#9745;" else "&#9744;"
                val spanCls = if (checked) "task-done" else ""
                htmlParts.add("<li class=\"task-item\"><span>$checkbox</span> <span class=\"$spanCls\">${parseInlineMarkdown(content)}</span></li>")
                i++
                continue
            }

            // Unordered list
            val ulMatch = Regex("^\\s*[-*]\\s+(.*)$").find(line)
            if (ulMatch != null) {
                if (!inList || listType != "ul") {
                    flushList()
                    inList = true
                    listType = "ul"
                    htmlParts.add("<ul class=\"$alignClass\" dir=\"$dirStr\">")
                }
                htmlParts.add("<li>${parseInlineMarkdown(ulMatch.groupValues[1])}</li>")
                i++
                continue
            }

            // Ordered list
            val olMatch = Regex("^\\s*\\d+\\.\\s+(.*)$").find(line)
            if (olMatch != null) {
                if (!inList || listType != "ol") {
                    flushList()
                    inList = true
                    listType = "ol"
                    htmlParts.add("<ol class=\"$alignClass\" dir=\"$dirStr\">")
                }
                htmlParts.add("<li>${parseInlineMarkdown(olMatch.groupValues[1])}</li>")
                i++
                continue
            }

            // Raw HTML tag line
            if (Regex("^<(p|div|img|figure|table|blockquote|h[1-6])\\b", RegexOption.IGNORE_CASE).containsMatchIn(trimmed)) {
                flushList()
                htmlParts.add(parseInlineMarkdown(line))
                i++
                continue
            }

            // Normal paragraph
            flushList()
            htmlParts.add("<p class=\"$alignClass\" dir=\"$dirStr\">${parseInlineMarkdown(line)}</p>")
            i++
        }

        flushList()
        flushBlockquote()
        flushTable()
        return htmlParts.joinToString("\n")
    }

    fun parseInlineMarkdown(text: String): String {
        if (text.isEmpty()) return ""
        var out = text

        // Images ![alt](src)
        out = Regex("!\\[([^\\]]*)\\]\\(([^)]+)\\)").replace(out) { m ->
            val alt = escapeHtml(m.groupValues[1].ifBlank { "Image" })
            val src = escapeHtml(m.groupValues[2])
            "<img src=\"$src\" alt=\"$alt\" class=\"doc-img\" />"
        }

        // Inline math $...$
        out = Regex("\\$([^$\\n]+)\\$").replace(out) { m ->
            val formula = m.groupValues[1].trim()
            "<span class=\"math-inline\">${LatexToOmml.renderLatexToPreviewHtml(formula, displayMode = false)}</span>"
        }

        // Inline math \(...\)
        out = Regex("\\\\\\(([^()\\n]+)\\\\\\)").replace(out) { m ->
            val formula = m.groupValues[1].trim()
            "<span class=\"math-inline\">${LatexToOmml.renderLatexToPreviewHtml(formula, displayMode = false)}</span>"
        }

        // Inline code `...`
        out = Regex("`([^`]+)`").replace(out) { m ->
            "<code class=\"inline-code\">${escapeHtml(m.groupValues[1])}</code>"
        }

        // Bold **text** or __text__
        out = Regex("\\*\\*([^*]+)\\*\\*").replace(out) { "<strong>${it.groupValues[1]}</strong>" }
        out = Regex("__([^_]+)__").replace(out) { "<strong>${it.groupValues[1]}</strong>" }

        // Italic *text* or _text_
        out = Regex("\\*([^*]+)\\*").replace(out) { "<em>${it.groupValues[1]}</em>" }
        out = Regex("_([^_]+)_").replace(out) { "<em>${it.groupValues[1]}</em>" }

        // Strikethrough ~~text~~
        out = Regex("~~([^~]+)~~").replace(out) { "<del>${it.groupValues[1]}</del>" }

        // Links [label](url)
        out = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)").replace(out) { m ->
            "<a href=\"${escapeHtml(m.groupValues[2])}\">${m.groupValues[1]}</a>"
        }

        return out
    }

    fun prepareGoogleDocsHtml(markdown: String, direction: TextDirectionMode): String {
        val body = markdownToHtmlFragment(markdown, direction)
        return """
            <!DOCTYPE html><html><head><meta charset="utf-8"><title>midadd Document</title></head>
            <body dir="${direction.wireValue}" style="font-family: 'Segoe UI', Roboto, Arial, sans-serif; font-size: 11pt; color: #1e293b; line-height: 1.6;">
            $body
            </body></html>
        """.trimIndent()
    }

    fun buildA4DocumentHtml(
        markdown: String,
        title: String,
        direction: TextDirectionMode,
        orientation: PageOrientation,
        margins: PageMargins,
        fontFamily: String,
        fontSizeSp: Int,
        watermark: String,
        zoomPercent: Int,
        darkSurface: Boolean
    ): String {
        val body = markdownToHtmlFragment(markdown, direction)
        val padPx = when (margins) {
            PageMargins.NARROW -> "16px"
            PageMargins.WIDE -> "38px"
            PageMargins.NORMAL -> "26px"
        }
        val bgPage = if (darkSurface) "#0f172a" else "#ffffff"
        val fgText = if (darkSurface) "#e2e8f0" else "#1e293b"
        val fgHeading = if (darkSurface) "#f8fafc" else "#0f172a"
        val borderColor = if (darkSurface) "#334155" else "#cbd5e1"
        val thBg = if (darkSurface) "#1e293b" else "#f1f5f9"
        val zebraBg = if (darkSurface) "#162032" else "#f8fafc"
        val scale = (zoomPercent.coerceIn(75, 150)) / 100.0
        val watermarkHtml = if (watermark.isNotBlank()) {
            "<div class=\"watermark\">${escapeHtml(watermark)}</div>"
        } else ""
        val minWidthCss = if (orientation == PageOrientation.LANDSCAPE) "min-width: 720px;" else ""

        return """
            <!DOCTYPE html>
            <html dir="${direction.wireValue}">
            <head>
              <meta charset="utf-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0" />
              <title>${escapeHtml(title)}</title>
              <style>
                * { box-sizing: border-box; }
                body {
                  margin: 0;
                  padding: 12px;
                  background: ${if (darkSurface) "#020617" else "#f1f5f9"};
                  font-family: '$fontFamily', 'Cairo', 'Amiri', sans-serif;
                  color: $fgText;
                  font-size: ${fontSizeSp}px;
                  line-height: 1.65;
                }
                .page {
                  position: relative;
                  background: $bgPage;
                  border: 1px solid $borderColor;
                  border-radius: 10px;
                  padding: $padPx;
                  margin: 0 auto;
                  $minWidthCss
                  transform: scale($scale);
                  transform-origin: top center;
                  overflow: hidden;
                  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.08);
                }
                .watermark {
                  position: fixed;
                  top: 42%;
                  left: 50%;
                  transform: translate(-50%, -50%) rotate(-24deg);
                  font-size: 40px;
                  font-weight: 800;
                  letter-spacing: 4px;
                  color: rgba(148, 163, 184, 0.18);
                  pointer-events: none;
                  z-index: 0;
                  white-space: nowrap;
                }
                .content { position: relative; z-index: 1; }
                h1 { font-size: 1.65em; font-weight: 800; color: $fgHeading; margin: 0.4em 0 0.5em; }
                h2 { font-size: 1.35em; font-weight: 700; color: $fgHeading; margin: 1em 0 0.4em; padding-bottom: 4px; border-bottom: 1px solid $borderColor; }
                h3 { font-size: 1.15em; font-weight: 700; color: $fgHeading; margin: 0.8em 0 0.35em; }
                h4 { font-size: 1.02em; font-weight: 600; color: $fgHeading; margin: 0.7em 0 0.3em; }
                p { margin: 0.55em 0; }
                hr { border: none; border-top: 1px solid $borderColor; margin: 1em 0; }
                .text-right { text-align: right; }
                .text-left { text-align: left; }
                blockquote {
                  border-inline-start: 4px solid #6366f1;
                  background: $zebraBg;
                  margin: 0.8em 0;
                  padding: 8px 12px;
                  border-radius: 6px;
                  font-style: italic;
                }
                .callout-warning { border-inline-start-color: #d97706; background: rgba(245, 158, 11, 0.12); font-style: normal; }
                .callout-note { border-inline-start-color: #2563eb; background: rgba(59, 130, 246, 0.10); font-style: normal; }
                .table-wrap { overflow-x: auto; margin: 0.9em 0; }
                .editor-table { width: 100%; border-collapse: collapse; font-size: 0.9em; }
                .editor-table th, .editor-table td { border: 1px solid $borderColor; padding: 8px 10px; }
                .editor-table th { background: $thBg; font-weight: 700; color: $fgHeading; }
                .editor-table tr.even td { background: $zebraBg; }
                .code-card {
                  margin: 0.9em 0;
                  border-radius: 8px;
                  border: 1px solid #334155;
                  background: #0f172a;
                  color: #f8fafc;
                  overflow: hidden;
                }
                .code-header {
                  display: flex;
                  justify-content: space-between;
                  padding: 6px 12px;
                  background: #1e293b;
                  color: #94a3b8;
                  font-size: 11px;
                  font-family: monospace;
                }
                .code-pre {
                  margin: 0;
                  padding: 12px;
                  overflow-x: auto;
                  font-size: 12px;
                  line-height: 1.5;
                  font-family: 'JetBrains Mono', monospace;
                }
                .inline-code {
                  font-family: monospace;
                  background: $thBg;
                  color: #e11d48;
                  padding: 1px 5px;
                  border-radius: 4px;
                  font-size: 0.88em;
                }
                .math-block {
                  margin: 0.9em 0;
                  padding: 10px 12px;
                  background: $zebraBg;
                  border: 1px solid $borderColor;
                  border-radius: 8px;
                  text-align: center;
                  overflow-x: auto;
                }
                .math-inline {
                  display: inline-block;
                  padding: 0 4px;
                  background: $thBg;
                  border-radius: 4px;
                }
                .math-expr { font-family: Georgia, 'Times New Roman', serif; font-size: 1.04em; }
                .math-display { font-size: 1.15em; }
                .mfrac { display: inline-flex; flex-direction: column; align-items: center; vertical-align: middle; margin: 0 3px; }
                .mnum { border-bottom: 1px solid currentColor; padding: 0 3px 1px; font-size: 0.86em; }
                .mden { padding: 1px 3px 0; font-size: 0.86em; }
                .msqrt .mrad { border-top: 1px solid currentColor; padding: 0 3px; }
                .mnary { display: inline-flex; flex-direction: column; align-items: center; vertical-align: middle; margin: 0 3px; line-height: 1; }
                .mnary-op { font-size: 1.25em; }
                .mlimit-top, .mlimit-bot { font-size: 0.65em; }
                ul, ol { margin: 0.6em 0; padding-inline-start: 22px; }
                li { margin: 4px 0; }
                .task-list { list-style: none; padding-inline-start: 4px; }
                .task-done { text-decoration: line-through; opacity: 0.6; }
                .doc-img { max-width: 100%; height: auto; border-radius: 8px; margin: 8px 0; border: 1px solid $borderColor; }
                .page-break { text-align: center; font-size: 11px; color: #94a3b8; border-top: 1px dashed $borderColor; margin: 16px 0; padding-top: 4px; }
                a { color: #4f46e5; text-decoration: underline; }
                @media print {
                  body { background: #ffffff !important; padding: 0 !important; color: #0f172a !important; }
                  .page { border: none !important; box-shadow: none !important; transform: none !important; }
                }
              </style>
            </head>
            <body>
              <div class="page">
                $watermarkHtml
                <div class="content">
                  ${if (body.isBlank()) "<p style=\"color:#94a3b8\">Start typing or insert AI content...</p>" else body}
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()
    }
}
