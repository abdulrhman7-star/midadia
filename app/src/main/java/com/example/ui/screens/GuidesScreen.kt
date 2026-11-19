package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.GuideArticle
import com.example.data.model.SupportedLanguage
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.viewmodel.MainViewModel

@Composable
fun GuidesScreen(
    viewModel: MainViewModel,
    language: SupportedLanguage,
    onInsertExampleToEditor: (String) -> Unit
) {
    val isAr = language == SupportedLanguage.AR
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    var selectedArticle by remember { mutableStateOf<GuideArticle?>(null) }

    selectedArticle?.let { article ->
        BackHandler { selectedArticle = null }
        GuideReaderView(
            article = article,
            isAr = isAr,
            onBack = { selectedArticle = null },
            onTrySnippetInEditor = { snippet ->
                onInsertExampleToEditor(snippet)
            }
        )
        return
    }

    val categories = remember(isAr) {
        listOf(
            "all" to if (isAr) "جميع الأدلة" else "All Guides",
            "formatting" to if (isAr) "إنتاجية المستندات" else "Document Workflows",
            "math" to if (isAr) "البحث العلمي والمعادلات" else "Academic & Math",
            "arabic-rtl" to if (isAr) "دعم العربية والـ RTL" else "Arabic & RTL Typography"
        )
    }

    val filtered = remember(viewModel.guides, searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        viewModel.guides.filter { g ->
            val catOk = selectedCategory == "all" || g.categoryId == selectedCategory
            val qOk = q.isEmpty() ||
                g.titleAr.lowercase().contains(q) ||
                g.titleEn.lowercase().contains(q) ||
                g.excerptAr.lowercase().contains(q) ||
                g.excerptEn.lowercase().contains(q)
            catOk && qOk
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isAr) "الأدلة والشروحات التقنية" else "Technical Guides & Tutorials",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            placeholder = { Text(if (isAr) "ابحث في الأدلة..." else "Search guides...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("guides_search_input")
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories, key = { it.first }) { (id, label) ->
                FilterChip(
                    selected = selectedCategory == id,
                    onClick = { selectedCategory = id },
                    label = { Text(label) }
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filtered, key = { it.slug }) { guide ->
                Card(
                    onClick = { selectedArticle = guide },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guide_card_${guide.slug}")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (isAr) guide.categoryAr else guide.categoryEn,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                                Text(
                                    text = "${guide.readingMinutes} min",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = if (isAr) guide.titleAr else guide.titleEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (isAr) guide.excerptAr else guide.excerptEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = if (isAr) guide.authorAr else guide.authorEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideReaderView(
    article: GuideArticle,
    isAr: Boolean,
    onBack: () -> Unit,
    onTrySnippetInEditor: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("guide_back_button")
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Rounded.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isAr) article.categoryAr else article.categoryEn,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = if (isAr) article.titleAr else article.titleEn,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${if (isAr) article.authorAr else article.authorEn} · ${article.publishedAt} · ${article.readingMinutes} min",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        article.sections.forEach { section ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isAr) section.headingAr else section.headingEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val paragraphs = if (isAr) section.paragraphsAr else section.paragraphsEn
                    paragraphs.forEach { p ->
                        Text(text = p, style = MaterialTheme.typography.bodyMedium)
                    }

                    val bullets = if (isAr) section.bulletsAr else section.bulletsEn
                    bullets.forEach { b ->
                        Text(
                            text = "• $b",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    section.codeExample?.let { code ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = code,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily)
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { onTrySnippetInEditor(code) },
                                    modifier = Modifier.testTag("guide_try_snippet_${section.id}")
                                ) {
                                    Text(if (isAr) "جرّب هذا المثال في المحرر" else "Try Snippet in Editor")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
