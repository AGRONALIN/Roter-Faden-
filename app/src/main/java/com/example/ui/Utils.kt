package com.example.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
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
