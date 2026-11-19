package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.PageMargins
import com.example.data.model.PageOrientation
import com.example.data.model.SupportedLanguage
import com.example.data.model.TextDirectionMode

@Composable
fun WordRuler(
    orientation: PageOrientation,
    margins: PageMargins,
    direction: TextDirectionMode,
    language: SupportedLanguage,
    modifier: Modifier = Modifier
) {
    val isAr = language == SupportedLanguage.AR
    val marginShadeColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
    val rulerBgColor = MaterialTheme.colorScheme.surface
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.colorScheme.primary

    val marginFraction = when (margins) {
        PageMargins.NARROW -> 0.06f
        PageMargins.WIDE -> 0.17f
        PageMargins.NORMAL -> 0.11f
    }

    val orientLabel = if (orientation == PageOrientation.LANDSCAPE) {
        if (isAr) "أفقي" else "Landscape"
    } else {
        if (isAr) "عمودي" else "Portrait"
    }
    val marginLabel = when (margins) {
        PageMargins.NARROW -> if (isAr) "هامش ضيق" else "Narrow"
        PageMargins.WIDE -> if (isAr) "هامش عريض" else "Wide"
        PageMargins.NORMAL -> if (isAr) "هامش عادي" else "Normal"
    }
    val dirLabel = if (direction == TextDirectionMode.RTL) "RTL" else "LTR"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("word_ruler"),
        color = rulerBgColor,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "A4 $orientLabel · $marginLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dirLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
            ) {
                val w = size.width
                val h = size.height
                val marginPx = w * marginFraction

                // Background
                drawRect(color = rulerBgColor, size = Size(w, h))

                // Margin shades on both sides
                drawRect(color = marginShadeColor, topLeft = Offset(0f, 0f), size = Size(marginPx, h))
                drawRect(color = marginShadeColor, topLeft = Offset(w - marginPx, 0f), size = Size(marginPx, h))

                // Tick marks across width
                val segments = 32
                val step = w / segments
                for (i in 0..segments) {
                    val x = i * step
                    val isMajor = i % 4 == 0
                    val tickHeight = if (isMajor) h * 0.55f else h * 0.28f
                    drawLine(
                        color = tickColor.copy(alpha = if (isMajor) 0.75f else 0.4f),
                        start = Offset(x, h),
                        end = Offset(x, h - tickHeight),
                        strokeWidth = if (isMajor) 2f else 1f,
                        cap = StrokeCap.Round
                    )
                }

                // Active margin markers
                drawLine(
                    color = accentColor,
                    start = Offset(marginPx, 0f),
                    end = Offset(marginPx, h),
                    strokeWidth = 3f
                )
                drawLine(
                    color = accentColor,
                    start = Offset(w - marginPx, 0f),
                    end = Offset(w - marginPx, h),
                    strokeWidth = 3f
                )
            }
        }
    }
}
