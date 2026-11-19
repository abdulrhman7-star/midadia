package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.i18n.AppStringsProvider
import com.example.data.model.SupportedLanguage
import com.example.data.model.TemplateItem
import com.example.data.repository.TemplatesRepository
import com.example.domain.export.AndroidExportHelper
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun TemplatesScreen(
    viewModel: MainViewModel,
    favoriteIds: List<String>,
    language: SupportedLanguage,
    onNavigateToEditor: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val strings = remember(language) { AppStringsProvider.forLanguage(language) }
    val isAr = language == SupportedLanguage.AR

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    var previewTemplate by remember { mutableStateOf<TemplateItem?>(null) }

    val filteredTemplates = remember(viewModel.templates, searchQuery, selectedCategory, favoriteIds, language) {
        val q = searchQuery.trim().lowercase()
        viewModel.templates.filter { tpl ->
            val catOk = when (selectedCategory) {
                "all" -> true
                "favorites" -> tpl.id in favoriteIds
                else -> tpl.category == selectedCategory
            }
            val queryOk = q.isEmpty() ||
                tpl.name.ar.lowercase().contains(q) ||
                tpl.name.en.lowercase().contains(q) ||
                tpl.desc.ar.lowercase().contains(q) ||
                tpl.desc.en.lowercase().contains(q) ||
                tpl.tags.any { it.lowercase().contains(q) }
            catOk && queryOk
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isAr) "مكتبة القوالب الاحترافية (${viewModel.templates.size})" else "Professional Templates (${viewModel.templates.size})",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            placeholder = {
                Text(if (isAr) "ابحث في 33 قالباً..." else "Search 33 templates...")
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("templates_search_input")
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(TemplatesRepository.CATEGORY_LABELS, key = { it.first }) { (catId, label) ->
                FilterChip(
                    selected = selectedCategory == catId,
                    onClick = { selectedCategory = catId },
                    label = { Text(if (isAr) label.ar else label.en) },
                    modifier = Modifier.testTag("template_cat_$catId")
                )
            }
        }

        if (filteredTemplates.isEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isAr) "لا توجد قوالب مطابقة لبحثك." else "No templates match your search.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredTemplates, key = { it.id }) { tpl ->
                    val isFav = tpl.id in favoriteIds
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("template_card_${tpl.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = tpl.icon.ifBlank { "DOC" },
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.toggleFavoriteTemplate(tpl.id) },
                                    modifier = Modifier.testTag("template_fav_${tpl.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFav) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = tpl.pickName(language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = tpl.pickDesc(language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.loadTemplateIntoEditor(tpl)
                                        onNavigateToEditor()
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("template_use_${tpl.id}")
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isAr) "استخدام" else "Use", style = MaterialTheme.typography.labelMedium)
                                }

                                OutlinedButton(
                                    onClick = { previewTemplate = tpl },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("template_preview_${tpl.id}")
                                ) {
                                    Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                }

                                OutlinedButton(
                                    onClick = {
                                        AndroidExportHelper.copyPlainText(context, tpl.pickName(language), tpl.pickMarkdown(language))
                                        viewModel.emitToast(if (isAr) "تم نسخ القالب" else "Template copied")
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("template_copy_${tpl.id}")
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                }

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            if (!viewModel.tryConsumeExportCredit()) return@launch
                                            val bytes = viewModel.buildTemplateDocxBytes(tpl)
                                            AndroidExportHelper.shareDocxFile(context, tpl.pickName(language), bytes)
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("template_docx_${tpl.id}")
                                ) {
                                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    previewTemplate?.let { tpl ->
        AlertDialog(
            onDismissRequest = { previewTemplate = null },
            title = { Text(tpl.pickName(language)) },
            text = {
                Column(
                    modifier = Modifier
                        .height(340.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = tpl.pickMarkdown(language),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.loadTemplateIntoEditor(tpl)
                    previewTemplate = null
                    onNavigateToEditor()
                }) {
                    Text(if (isAr) "فتح في المحرر" else "Open in Editor")
                }
            },
            dismissButton = {
                TextButton(onClick = { previewTemplate = null }) { Text(strings.close) }
            }
        )
    }
}
