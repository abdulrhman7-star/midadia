package com.example.domain.markdown

object LatexToOmml {
    val LATEX_SYMBOLS: Map<String, String> = mapOf(
        "\\alpha" to "α",
        "\\beta" to "β",
        "\\gamma" to "γ",
        "\\delta" to "δ",
        "\\epsilon" to "ε",
        "\\varepsilon" to "ε",
        "\\zeta" to "ζ",
        "\\eta" to "η",
        "\\theta" to "θ",
        "\\vartheta" to "ϑ",
        "\\iota" to "ι",
        "\\kappa" to "κ",
        "\\lambda" to "λ",
        "\\mu" to "μ",
        "\\nu" to "ν",
        "\\xi" to "ξ",
        "\\pi" to "π",
        "\\rho" to "ρ",
        "\\sigma" to "σ",
        "\\tau" to "τ",
        "\\upsilon" to "υ",
        "\\phi" to "φ",
        "\\varphi" to "ϕ",
        "\\chi" to "χ",
        "\\psi" to "ψ",
        "\\omega" to "ω",
        "\\Gamma" to "Γ",
        "\\Delta" to "Δ",
        "\\Theta" to "Θ",
        "\\Lambda" to "Λ",
        "\\Xi" to "Ξ",
        "\\Pi" to "Π",
        "\\Sigma" to "Σ",
        "\\Upsilon" to "Υ",
        "\\Phi" to "Φ",
        "\\Psi" to "Ψ",
        "\\Omega" to "Ω",
        "\\pm" to "±",
        "\\mp" to "∓",
        "\\times" to "×",
        "\\div" to "÷",
        "\\cdot" to "·",
        "\\ast" to "*",
        "\\star" to "★",
        "\\circ" to "∘",
        "\\bullet" to "•",
        "\\leq" to "≤",
        "\\le" to "≤",
        "\\geq" to "≥",
        "\\ge" to "≥",
        "\\neq" to "≠",
        "\\ne" to "≠",
        "\\approx" to "≈",
        "\\sim" to "~",
        "\\equiv" to "≡",
        "\\infty" to "∞",
        "\\in" to "∈",
        "\\notin" to "∉",
        "\\subset" to "⊂",
        "\\supset" to "⊃",
        "\\subseteq" to "⊆",
        "\\supseteq" to "⊇",
        "\\cup" to "∪",
        "\\cap" to "∩",
        "\\forall" to "∀",
        "\\exists" to "∃",
        "\\nexists" to "∄",
        "\\partial" to "∂",
        "\\nabla" to "∇",
        "\\to" to "→",
        "\\rightarrow" to "→",
        "\\leftarrow" to "←",
        "\\Rightarrow" to "⇒",
        "\\Leftarrow" to "⇐",
        "\\leftrightarrow" to "↔",
        "\\Leftrightarrow" to "⇔",
        "\\langle" to "⟨",
        "\\rangle" to "⟩",
        "\\dots" to "…",
        "\\cdots" to "⋯",
        "\\quad" to "  ",
        "\\qquad" to "    ",
        "\\," to " ",
        "\\;" to " ",
        "\\!" to ""
    )

    private val FUNC_NAMES = setOf(
        "\\sin", "\\cos", "\\tan", "\\log", "\\ln", "\\lim", "\\max", "\\min", "\\exp", "\\det", "\\text", "\\mathrm", "\\mathbf"
    )

    private data class GroupMatch(val content: String, val nextIndex: Int)

    private fun extractBracedGroup(str: String, startIndex: Int): GroupMatch? {
        if (startIndex >= str.length) return null
        if (str[startIndex] != '{') {
            if (str[startIndex] == '\\') {
                val match = Regex("^\\\\[a-zA-Z]+").find(str.substring(startIndex))
                if (match != null) {
                    return GroupMatch(match.value, startIndex + match.value.length)
                }
            }
            return GroupMatch(str[startIndex].toString(), startIndex + 1)
        }
        var depth = 0
        val start = startIndex + 1
        for (i in startIndex until str.length) {
            if (str[i] == '{') depth++
            else if (str[i] == '}') {
                depth--
                if (depth == 0) {
                    return GroupMatch(str.substring(start, i), i + 1)
                }
            }
        }
        return GroupMatch(str.substring(start), str.length)
    }

    private fun stripDelimiters(latex: String): String {
        val t = latex.trim()
        return when {
            t.startsWith("$$") && t.endsWith("$$") && t.length >= 4 -> t.substring(2, t.length - 2).trim()
            t.startsWith("$") && t.endsWith("$") && t.length >= 2 -> t.substring(1, t.length - 1).trim()
            t.startsWith("\\[") && t.endsWith("\\]") && t.length >= 4 -> t.substring(2, t.length - 2).trim()
            t.startsWith("\\(") && t.endsWith("\\)") && t.length >= 4 -> t.substring(2, t.length - 2).trim()
            else -> t
        }
    }

    fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private fun mathRun(text: String): String =
        "<m:r><m:t xml:space=\"preserve\">${escapeXml(text)}</m:t></m:r>"

    /**
     * Converts a LaTeX expression to Office Math Markup Language (<m:oMath>) XML nodes
     * compatible with Microsoft Word (.docx).
     */
    fun toOmmlParagraphElement(latex: String): String {
        val inner = parseLatexToOmmlNodes(latex)
        return "<m:oMath>$inner</m:oMath>"
    }

    fun parseLatexToOmmlNodes(latex: String): String {
        val cleaned = stripDelimiters(latex)
        if (cleaned.isEmpty()) return mathRun(" ")

        val components = mutableListOf<String>()
        var i = 0

        while (i < cleaned.length) {
            val ch = cleaned[i]
            if (ch.isWhitespace()) {
                components.add(mathRun(" "))
                i++
                continue
            }

            // \frac{num}{den}
            if (cleaned.startsWith("\\frac", i)) {
                i += 5
                while (i < cleaned.length && cleaned[i].isWhitespace()) i++
                val num = extractBracedGroup(cleaned, i)
                if (num != null) {
                    i = num.nextIndex
                    while (i < cleaned.length && cleaned[i].isWhitespace()) i++
                    val den = extractBracedGroup(cleaned, i)
                    if (den != null) {
                        i = den.nextIndex
                        val numXml = parseLatexToOmmlNodes(num.content)
                        val denXml = parseLatexToOmmlNodes(den.content)
                        components.add("<m:f><m:num>$numXml</m:num><m:den>$denXml</m:den></m:f>")
                        continue
                    }
                }
            }

            // \sqrt{arg}
            if (cleaned.startsWith("\\sqrt", i)) {
                i += 5
                while (i < cleaned.length && cleaned[i].isWhitespace()) i++
                var degreeXml = "<m:degHide m:val=\"1\"/>"
                var degContent: String? = null
                if (i < cleaned.length && cleaned[i] == '[') {
                    val close = cleaned.indexOf(']', i)
                    if (close != -1) {
                        degContent = cleaned.substring(i + 1, close)
                        i = close + 1
                    }
                }
                val arg = extractBracedGroup(cleaned, i)
                if (arg != null) {
                    i = arg.nextIndex
                    val degBlock = if (!degContent.isNullOrBlank()) {
                        "<m:deg>${parseLatexToOmmlNodes(degContent)}</m:deg>"
                    } else {
                        "<m:deg/>"
                    }
                    val pr = if (!degContent.isNullOrBlank()) "" else "<m:radPr>$degreeXml</m:radPr>"
                    components.add("<m:rad>$pr$degBlock<m:e>${parseLatexToOmmlNodes(arg.content)}</m:e></m:rad>")
                    continue
                }
            }

            // \sum_{a}^{b} or \int_{a}^{b}
            if (cleaned.startsWith("\\sum", i) || cleaned.startsWith("\\int", i)) {
                val isSum = cleaned.startsWith("\\sum", i)
                i += 4
                while (i < cleaned.length && cleaned[i].isWhitespace()) i++
                var subContent = ""
                var supContent = ""
                if (i < cleaned.length && cleaned[i] == '_') {
                    i++
                    extractBracedGroup(cleaned, i)?.let {
                        subContent = it.content
                        i = it.nextIndex
                    }
                }
                while (i < cleaned.length && cleaned[i].isWhitespace()) i++
                if (i < cleaned.length && cleaned[i] == '^') {
                    i++
                    extractBracedGroup(cleaned, i)?.let {
                        supContent = it.content
                        i = it.nextIndex
                    }
                }
                val chr = if (isSum) "∑" else "∫"
                val subXml = if (subContent.isNotEmpty()) parseLatexToOmmlNodes(subContent) else ""
                val supXml = if (supContent.isNotEmpty()) parseLatexToOmmlNodes(supContent) else ""
                components.add(
                    "<m:nary><m:naryPr><m:chr m:val=\"$chr\"/></m:naryPr>" +
                        "<m:sub>$subXml</m:sub><m:sup>$supXml</m:sup><m:e>${mathRun(" ")}</m:e></m:nary>"
                )
                continue
            }

            // Commands
            if (ch == '\\') {
                val cmdMatch = Regex("^\\\\[a-zA-Z,;!]+").find(cleaned.substring(i))
                if (cmdMatch != null) {
                    val cmd = cmdMatch.value
                    i += cmd.length
                    val sym = LATEX_SYMBOLS[cmd]
                    if (sym != null) {
                        components.add(mathRun(sym))
                        continue
                    }
                    if (cmd == "\\left" || cmd == "\\right") {
                        continue
                    }
                    if (cmd == "\\text" || cmd == "\\mathrm" || cmd == "\\mathbf") {
                        while (i < cleaned.length && cleaned[i].isWhitespace()) i++
                        val grp = extractBracedGroup(cleaned, i)
                        if (grp != null) {
                            i = grp.nextIndex
                            components.add(mathRun(grp.content))
                            continue
                        }
                    }
                    if (cmd in FUNC_NAMES) {
                        components.add(mathRun("${cmd.drop(1)} "))
                        continue
                    }
                    components.add(mathRun(cmd.drop(1)))
                    continue
                }
            }

            // Superscript ^
            if (ch == '^') {
                i++
                val supGroup = extractBracedGroup(cleaned, i)
                if (supGroup != null) {
                    i = supGroup.nextIndex
                    val base = if (components.isNotEmpty()) components.removeAt(components.lastIndex) else mathRun("")
                    components.add(
                        "<m:sSup><m:e>$base</m:e><m:sup>${parseLatexToOmmlNodes(supGroup.content)}</m:sup></m:sSup>"
                    )
                    continue
                }
            }

            // Subscript _
            if (ch == '_') {
                i++
                val subGroup = extractBracedGroup(cleaned, i)
                if (subGroup != null) {
                    i = subGroup.nextIndex
                    val base = if (components.isNotEmpty()) components.removeAt(components.lastIndex) else mathRun("")
                    var supContent = ""
                    if (i < cleaned.length && cleaned[i] == '^') {
                        i++
                        extractBracedGroup(cleaned, i)?.let {
                            supContent = it.content
                            i = it.nextIndex
                        }
                    }
                    if (supContent.isNotEmpty()) {
                        components.add(
                            "<m:sSubSup><m:e>$base</m:e>" +
                                "<m:sub>${parseLatexToOmmlNodes(subGroup.content)}</m:sub>" +
                                "<m:sup>${parseLatexToOmmlNodes(supContent)}</m:sup></m:sSubSup>"
                        )
                    } else {
                        components.add(
                            "<m:sSub><m:e>$base</m:e><m:sub>${parseLatexToOmmlNodes(subGroup.content)}</m:sub></m:sSub>"
                        )
                    }
                    continue
                }
            }

            // Parentheses grouping (a + b)
            if (ch == '(') {
                var depth = 0
                var foundEnd = -1
                for (j in i until cleaned.length) {
                    if (cleaned[j] == '(') depth++
                    else if (cleaned[j] == ')') {
                        depth--
                        if (depth == 0) {
                            foundEnd = j
                            break
                        }
                    }
                }
                if (foundEnd != -1) {
                    val inner = cleaned.substring(i + 1, foundEnd)
                    components.add(
                        "<m:d><m:dPr><m:begChr m:val=\"(\"/><m:endChr m:val=\")\"/></m:dPr>" +
                            "<m:e>${parseLatexToOmmlNodes(inner)}</m:e></m:d>"
                    )
                    i = foundEnd + 1
                    continue
                }
            }

            components.add(mathRun(ch.toString()))
            i++
        }

        return if (components.isNotEmpty()) components.joinToString("") else mathRun(cleaned)
    }

    /**
     * Renders LaTeX to clean offline HTML/CSS math markup for WebView preview & PDF print.
     */
    fun renderLatexToPreviewHtml(latex: String, displayMode: Boolean = false): String {
        val inner = parseLatexToHtmlNodes(latex)
        return if (displayMode) {
            "<span class=\"math-expr math-display\" dir=\"ltr\">$inner</span>"
        } else {
            "<span class=\"math-expr\" dir=\"ltr\">$inner</span>"
        }
    }

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun parseLatexToHtmlNodes(latex: String): String {
        val cleaned = stripDelimiters(latex)
        if (cleaned.isEmpty()) return ""

        val parts = mutableListOf<String>()
        var i = 0
        while (i < cleaned.length) {
            val ch = cleaned[i]
            if (ch.isWhitespace()) {
                parts.add(" ")
                i++
                continue
            }

            if (cleaned.startsWith("\\frac", i)) {
                i += 5
                while (i < cleaned.length && cleaned[i].isWhitespace()) i++
                val num = extractBracedGroup(cleaned, i)
                if (num != null) {
                    i = num.nextIndex
                    while (i < cleaned.length && cleaned[i].isWhitespace()) i++
                    val den = extractBracedGroup(cleaned, i)
                    if (den != null) {
                        i = den.nextIndex
                        parts.add(
                            "<span class=\"mfrac\"><span class=\"mnum\">${parseLatexToHtmlNodes(num.content)}</span>" +
                                "<span class=\"mden\">${parseLatexToHtmlNodes(den.content)}</span></span>"
                        )
                        continue
                    }
                }
            }

            if (cleaned.startsWith("\\sqrt", i)) {
                i += 5
                while (i < cleaned.length && cleaned[i].isWhitespace()) i++
                var deg = ""
                if (i < cleaned.length && cleaned[i] == '[') {
                    val close = cleaned.indexOf(']', i)
                    if (close != -1) {
                        deg = "<sup class=\"mdeg\">${escapeHtml(cleaned.substring(i + 1, close))}</sup>"
                        i = close + 1
                    }
                }
                val arg = extractBracedGroup(cleaned, i)
                if (arg != null) {
                    i = arg.nextIndex
                    parts.add("$deg<span class=\"msqrt\">&radic;<span class=\"mrad\">${parseLatexToHtmlNodes(arg.content)}</span></span>")
                    continue
                }
            }

            if (cleaned.startsWith("\\sum", i) || cleaned.startsWith("\\int", i)) {
                val isSum = cleaned.startsWith("\\sum", i)
                i += 4
                while (i < cleaned.length && cleaned[i].isWhitespace()) i++
                var sub = ""
                var sup = ""
                if (i < cleaned.length && cleaned[i] == '_') {
                    i++
                    extractBracedGroup(cleaned, i)?.let {
                        sub = it.content
                        i = it.nextIndex
                    }
                }
                while (i < cleaned.length && cleaned[i].isWhitespace()) i++
                if (i < cleaned.length && cleaned[i] == '^') {
                    i++
                    extractBracedGroup(cleaned, i)?.let {
                        sup = it.content
                        i = it.nextIndex
                    }
                }
                val sym = if (isSum) "&sum;" else "&int;"
                val supHtml = if (sup.isNotEmpty()) "<span class=\"mlimit-top\">${parseLatexToHtmlNodes(sup)}</span>" else ""
                val subHtml = if (sub.isNotEmpty()) "<span class=\"mlimit-bot\">${parseLatexToHtmlNodes(sub)}</span>" else ""
                parts.add("<span class=\"mnary\">$supHtml<span class=\"mnary-op\">$sym</span>$subHtml</span>")
                continue
            }

            if (ch == '\\') {
                val cmdMatch = Regex("^\\\\[a-zA-Z,;!]+").find(cleaned.substring(i))
                if (cmdMatch != null) {
                    val cmd = cmdMatch.value
                    i += cmd.length
                    LATEX_SYMBOLS[cmd]?.let {
                        parts.add(escapeHtml(it))
                        continue
                    }
                    if (cmd == "\\left" || cmd == "\\right") continue
                    if (cmd == "\\text" || cmd == "\\mathrm" || cmd == "\\mathbf") {
                        while (i < cleaned.length && cleaned[i].isWhitespace()) i++
                        extractBracedGroup(cleaned, i)?.let {
                            i = it.nextIndex
                            parts.add("<span class=\"mtext\">${escapeHtml(it.content)}</span>")
                            continue
                        }
                    }
                    if (cmd in FUNC_NAMES) {
                        parts.add("<span class=\"mfunc\">${escapeHtml(cmd.drop(1))}</span> ")
                        continue
                    }
                    parts.add(escapeHtml(cmd.drop(1)))
                    continue
                }
            }

            if (ch == '^') {
                i++
                val g = extractBracedGroup(cleaned, i)
                if (g != null) {
                    i = g.nextIndex
                    parts.add("<sup>${parseLatexToHtmlNodes(g.content)}</sup>")
                    continue
                }
            }

            if (ch == '_') {
                i++
                val g = extractBracedGroup(cleaned, i)
                if (g != null) {
                    i = g.nextIndex
                    parts.add("<sub>${parseLatexToHtmlNodes(g.content)}</sub>")
                    continue
                }
            }

            if (ch == '{' || ch == '}') {
                i++
                continue
            }

            parts.add(escapeHtml(ch.toString()))
            i++
        }
        return parts.joinToString("")
    }
}
