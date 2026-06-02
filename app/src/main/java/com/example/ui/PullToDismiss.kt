package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch

@Composable
fun PullToDismissContainer(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(nestedScrollConnection: NestedScrollConnection) -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    var hasPopped by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0 && offsetY > 0) {
                    val consumed = minOf(-delta, offsetY)
                    offsetY -= consumed
                    return Offset(0f, -consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 0) {
                    offsetY += available.y
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offsetY > 150f && !hasPopped) {
                    hasPopped = true
                    onDismiss()
                    return available
                } else if (offsetY > 0) {
                    scope.launch {
                        animate(
                            initialValue = offsetY,
                            targetValue = 0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        ) { value, _ ->
                            offsetY = value
                        }
                    }
                    return available
                }
                return super.onPreFling(available)
            }
        }
    }

    val scaleRatio = 1f - (offsetY / 2500f).coerceIn(0f, 0.15f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, offsetY.toInt()) }
            .scale(scaleRatio)
    ) {
        content(nestedScrollConnection)
    }
}
