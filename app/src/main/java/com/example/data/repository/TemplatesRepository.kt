package com.example.data.repository

import android.content.Context
import com.example.data.model.LocalizedText
import com.example.data.model.TemplateItem
import kotlinx.serialization.json.Json

object TemplatesRepository {
    private val json = Json { ignoreUnknownKeys = true }

    val CATEGORY_LABELS: List<Pair<String, LocalizedText>> = listOf(
        "all" to LocalizedText(ar = "الكل", en = "All"),
        "favorites" to LocalizedText(ar = "⭐ المفضلة", en = "⭐ Favorites"),
        "academic" to LocalizedText(ar = "أكاديمي", en = "Academic"),
        "engineering" to LocalizedText(ar = "هندسة", en = "Engineering"),
        "tech" to LocalizedText(ar = "حاسوب/تقنية", en = "Tech"),
        "medical" to LocalizedText(ar = "طبي/صحي", en = "Medical"),
        "business" to LocalizedText(ar = "أعمال", en = "Business"),
        "reports" to LocalizedText(ar = "تقارير", en = "Reports"),
        "research" to LocalizedText(ar = "بحث", en = "Research"),
        "law" to LocalizedText(ar = "قانون", en = "Law"),
        "education" to LocalizedText(ar = "تربية/تعليم", en = "Education"),
        "media" to LocalizedText(ar = "إعلام", en = "Media"),
        "architecture" to LocalizedText(ar = "عمارة", en = "Architecture"),
        "science" to LocalizedText(ar = "علوم", en = "Science"),
        "pm" to LocalizedText(ar = "إدارة مشاريع", en = "Project Mgmt"),
        "ops" to LocalizedText(ar = "تشغيل", en = "Operations"),
        "analysis" to LocalizedText(ar = "تحليل", en = "Analysis"),
        "entrepreneur" to LocalizedText(ar = "ريادة", en = "Entrepreneur"),
        "projects" to LocalizedText(ar = "مشاريع", en = "Projects"),
        "jobs" to LocalizedText(ar = "وظائف", en = "Jobs"),
        "events" to LocalizedText(ar = "فعاليات", en = "Events"),
        "presentations" to LocalizedText(ar = "عروض", en = "Presentations")
    )

    fun loadTemplates(context: Context): List<TemplateItem> {
        return try {
            val raw = context.assets.open("templates.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            json.decodeFromString<List<TemplateItem>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
