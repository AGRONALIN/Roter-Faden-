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
        val topPx = topEdge.toPx().coerceAtMost(size.height / 2f)
        val bottomPx = bottomEdge.toPx().coerceAtMost(size.height / 2f)
        
        val colors = mutableListOf<Pair<Float, Color>>()
        if (topPx > 0f) {
            colors.add(0f to Color.Transparent)
            colors.add(topPx / size.height to Color.Black)
        } else {
            colors.add(0f to Color.Black)
        }
        
        if (bottomPx > 0f) {
            colors.add(1f - (bottomPx / size.height) to Color.Black)
            colors.add(1f to Color.Transparent)
        } else {
            colors.add(1f to Color.Black)
        }
        
        drawRect(
            brush = Brush.verticalGradient(
                *colors.toTypedArray(),
                startY = 0f,
                endY = size.height
            ),
            blendMode = BlendMode.DstIn
        )
    }
