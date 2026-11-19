package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable

enum class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String,
    val isRtl: Boolean
) {
    AR("ar", "Arabic", "العربية", "🇸🇦", true),
    EN("en", "English", "English", "🇺🇸", false),
    FR("fr", "French", "Français", "🇫🇷", false),
    ES("es", "Spanish", "Español", "🇪🇸", false),
    TR("tr", "Turkish", "Türkçe", "🇹🇷", false),
    DE("de", "German", "Deutsch", "🇩🇪", false),
    ZH("zh", "Chinese", "中文", "🇨🇳", false);

    companion object {
        fun fromCode(code: String?): SupportedLanguage =
            entries.firstOrNull { it.code == code } ?: AR
    }
}

enum class PageOrientation(val wireValue: String) {
    PORTRAIT("portrait"),
    LANDSCAPE("landscape");

    companion object {
        fun fromWire(value: String?): PageOrientation =
            entries.firstOrNull { it.wireValue == value } ?: PORTRAIT
    }
}

enum class PageMargins(val wireValue: String) {
    NORMAL("normal"),
    NARROW("narrow"),
    WIDE("wide");

    companion object {
        fun fromWire(value: String?): PageMargins =
            entries.firstOrNull { it.wireValue == value } ?: NORMAL
    }
}

enum class TextDirectionMode(val wireValue: String) {
    RTL("rtl"),
    LTR("ltr");

    companion object {
        fun fromWire(value: String?): TextDirectionMode =
            entries.firstOrNull { it.wireValue == value } ?: RTL
    }
}

enum class EditorViewMode {
    SPLIT,
    PRINT,
    SOURCE
}

enum class RibbonTab {
    HOME,
    INSERT,
    LAYOUT,
    REVIEW,
    VIEW,
    FILE
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    val userId: String = "",
    val displayName: String = "User",
    val email: String = "",
    val credits: Long = 25L,
    val maxCredits: Long = 25L,
    val preferredLanguage: String = "ar",
    val darkMode: Boolean = false,
    val favoriteTemplateIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SavedDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "Untitled Document",
    val markdown: String = "",
    val direction: String = "rtl",
    val orientation: String = "portrait",
    val margins: String = "normal",
    val fontFamily: String = "Cairo",
    val fontSizeSp: Long = 14L,
    val watermark: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Serializable
data class LocalizedText(
    val ar: String = "",
    val en: String = ""
)

@Serializable
data class TemplateItem(
    val id: String = "",
    val icon: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(),
    val name: LocalizedText = LocalizedText(),
    val desc: LocalizedText = LocalizedText(),
    val markdown: LocalizedText = LocalizedText()
) {
    fun pickName(lang: SupportedLanguage): String =
        if (lang != SupportedLanguage.AR && name.en.isNotBlank()) name.en else name.ar

    fun pickDesc(lang: SupportedLanguage): String =
        if (lang != SupportedLanguage.AR && desc.en.isNotBlank()) desc.en else desc.ar

    fun pickMarkdown(lang: SupportedLanguage): String =
        if (lang != SupportedLanguage.AR && markdown.en.isNotBlank()) markdown.en else markdown.ar
}

data class MarkdownPreset(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val markdown: String
)

data class GuideSection(
    val id: String,
    val headingAr: String,
    val headingEn: String,
    val paragraphsAr: List<String>,
    val paragraphsEn: List<String>,
    val codeExample: String? = null,
    val bulletsAr: List<String> = emptyList(),
    val bulletsEn: List<String> = emptyList()
)

data class GuideArticle(
    val slug: String,
    val categoryId: String,
    val categoryAr: String,
    val categoryEn: String,
    val publishedAt: String,
    val readingMinutes: Int,
    val titleAr: String,
    val titleEn: String,
    val excerptAr: String,
    val excerptEn: String,
    val authorAr: String,
    val authorEn: String,
    val sections: List<GuideSection>
)

data class DocumentStats(
    val words: Int,
    val charsWithSpaces: Int,
    val charsNoSpaces: Int,
    val paragraphs: Int,
    val sentences: Int,
    val lines: Int,
    val readingMinutes: Int,
    val speakingMinutes: Int,
    val mathCount: Int,
    val codeBlockCount: Int,
    val hasTables: Boolean
)
