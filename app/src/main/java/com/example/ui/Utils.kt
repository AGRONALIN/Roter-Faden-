package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.verticalFadingEdge(
    topEdge: Dp = 0.dp,
    bottomEdge: Dp = 0.dp
) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val height = size.height
        if (height <= 0f) return@drawWithContent
        
        val topPx = topEdge.toPx().coerceAtMost(height / 2f)
        val bottomPx = bottomEdge.toPx().coerceAtMost(height / 2f)
        
        if (topPx > 0f && bottomPx > 0f) {
            val stop1 = topPx / height
            val stop2 = 1f - (bottomPx / height)
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    stop1 to Color.Black,
                    stop2 to Color.Black,
                    1f to Color.Transparent,
                    startY = 0f,
                    endY = height
                ),
                blendMode = BlendMode.DstIn
            )
        } else if (topPx > 0f) {
            val stop1 = topPx / height
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    stop1 to Color.Black,
                    1f to Color.Black,
                    startY = 0f,
                    endY = height
                ),
                blendMode = BlendMode.DstIn
            )
        } else if (bottomPx > 0f) {
            val stop2 = 1f - (bottomPx / height)
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Black,
                    stop2 to Color.Black,
                    1f to Color.Transparent,
                    startY = 0f,
                    endY = height
                ),
                blendMode = BlendMode.DstIn
            )
        } else {
            drawRect(
                color = Color.Black,
                blendMode = BlendMode.DstIn
            )
        }
    }

fun buildAutoLinkedText(
    text: String,
    glossaryItems: List<com.example.data.GlossaryItem>,
    literatureItems: List<com.example.data.LiteratureItem>,
    highlightColor: Color,
    literatureColor: Color = Color(0xFF2E7D32),
    navController: androidx.navigation.NavController
): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        append(text)
        
        data class MatchRange(val start: Int, val end: Int, val route: String, val color: Color)
        val matches = mutableListOf<MatchRange>()
        
        val sortedLit = literatureItems.sortedByDescending { it.title.length }
        val sortedGlossary = glossaryItems.sortedByDescending { it.term.length }
        
        for (lit in sortedLit) {
            val title = lit.title
            if (title.isBlank()) continue
            var startIndex = text.indexOf(title, ignoreCase = true)
            while (startIndex >= 0) {
                val endIndex = startIndex + title.length
                val hasOverlap = matches.any { 
                    (startIndex >= it.start && startIndex < it.end) || 
                    (endIndex > it.start && endIndex <= it.end) ||
                    (it.start >= startIndex && it.start < endIndex)
                }
                if (!hasOverlap) {
                    matches.add(MatchRange(startIndex, endIndex, "literature_detail/${lit.id}?source=text", literatureColor))
                }
                startIndex = text.indexOf(title, startIndex + title.length, ignoreCase = true)
            }
        }
        
        for (glossary in sortedGlossary) {
            val term = glossary.term
            if (term.isBlank()) continue
            var startIndex = text.indexOf(term, ignoreCase = true)
            while (startIndex >= 0) {
                val endIndex = startIndex + term.length
                val hasOverlap = matches.any { 
                    (startIndex >= it.start && startIndex < it.end) || 
                    (endIndex > it.start && endIndex <= it.end) ||
                    (it.start >= startIndex && it.start < endIndex)
                }
                if (!hasOverlap) {
                    matches.add(MatchRange(startIndex, endIndex, "glossary_detail/${glossary.id}?source=text", highlightColor))
                }
                startIndex = text.indexOf(term, startIndex + term.length, ignoreCase = true)
            }
        }
        
        for (match in matches) {
            addStyle(
                style = androidx.compose.ui.text.SpanStyle(
                    color = match.color,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                ),
                start = match.start,
                end = match.end
            )
            addLink(
                androidx.compose.ui.text.LinkAnnotation.Clickable(match.route) {
                    navController.navigate(match.route)
                },
                start = match.start,
                end = match.end
            )
        }
    }
}

fun copyUriToInternalStorage(context: android.content.Context, uri: android.net.Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val outputFile = java.io.File(context.filesDir, fileName)
        outputFile.outputStream().use { outputStream ->
            inputStream.use { it.copyTo(outputStream) }
        }
        outputFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun LocalImageFromPath(path: String, modifier: Modifier) {
    val bitmap = remember(path) {
        try {
            android.graphics.BitmapFactory.decodeFile(path)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap,
            contentDescription = "Eingefügtes Bild",
            modifier = modifier,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}
