package com.example.domain.markdown

import com.example.data.model.DocumentStats
import kotlin.math.ceil
import kotlin.math.max

object DocumentStatsCalculator {
    fun calculate(markdown: String): DocumentStats {
        val cleanText = markdown.replace(Regex("[#*`~>|\\[\\]\\-_$]"), " ")
        val words = cleanText.trim().split(Regex("\\s+")).count { it.isNotBlank() }
        val charsWithSpaces = markdown.length
        val charsNoSpaces = markdown.replace(Regex("\\s"), "").length
        val paragraphs = markdown.split(Regex("\\n\\s*\\n")).count { it.trim().isNotEmpty() }
        val sentences = markdown.split(Regex("[.!?]+")).count { it.trim().isNotEmpty() }
        val lines = if (markdown.isEmpty()) 0 else markdown.split('\n').size

        val readingMinutes = max(1, ceil(words / 200.0).toInt())
        val speakingMinutes = max(1, ceil(words / 130.0).toInt())

        val hasTables = Regex("\\|[\\s\\S]*?\\|").containsMatchIn(markdown)
        val mathCount = Regex("\\$\\$[\\s\\S]*?\\$\\$|\\$[^$\\n]+\\$").findAll(markdown).count()
        val codeBlockCount = Regex("```[\\s\\S]*?```").findAll(markdown).count()

        return DocumentStats(
            words = words,
            charsWithSpaces = charsWithSpaces,
            charsNoSpaces = charsNoSpaces,
            paragraphs = paragraphs,
            sentences = sentences,
            lines = lines,
            readingMinutes = readingMinutes,
            speakingMinutes = speakingMinutes,
            mathCount = mathCount,
            codeBlockCount = codeBlockCount,
            hasTables = hasTables
        )
    }
}
