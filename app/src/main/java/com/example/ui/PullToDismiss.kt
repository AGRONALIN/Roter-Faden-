package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs
import kotlin.math.absoluteValue

@Composable
fun PullToDismissContainer(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(nestedScrollConnection: NestedScrollConnection) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val offsetX = remember { Animatable(0f) }
    
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.toDp().toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.toDp().toPx() }
    
    var isDragging by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                // If we are currently dragging up but we have a positive offset (pulled down), consume it
                if (offsetY.value > 0 && dy < 0) {
                    val newOffset = (offsetY.value + dy).coerceAtLeast(0f)
                    coroutineScope.launch {
                        offsetY.snapTo(newOffset)
                    }
                    return Offset(0f, dy)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offsetY.value > screenHeightPx * 0.30f || abs(offsetX.value) > screenWidthPx * 0.40f || (available.y > 3500f && offsetY.value > screenHeightPx * 0.08f)) {
                    onDismiss()
                    return available
                } else {
                    coroutineScope.launch {
                        offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessVeryLow))
                    }
                    coroutineScope.launch {
                        offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessVeryLow))
                    }
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        if (offsetY.value > screenHeightPx * 0.30f || abs(offsetX.value) > screenWidthPx * 0.40f) {
                            onDismiss()
                        } else {
                            coroutineScope.launch {
                                offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessVeryLow))
                            }
                            coroutineScope.launch {
                                offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessVeryLow))
                            }
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        coroutineScope.launch {
                            offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessVeryLow))
                        }
                        coroutineScope.launch {
                            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessVeryLow))
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            // Damped drag
                            val newY = offsetY.value + dragAmount.y * 0.6f
                            if (newY > 0) {
                                offsetY.snapTo(newY)
                            } else {
                                offsetY.snapTo(newY * 0.5f) // resistance
                            }
                            offsetX.snapTo(offsetX.value + dragAmount.x * 0.6f)
                        }
                    }
                )
            }
    ) {
        val scale = if (isDragging || offsetY.value > 0) {
            1f - (abs(offsetY.value) / screenHeightPx * 0.2f).coerceIn(0f, 0.2f)
        } else 1f

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
                .scale(scale)
        ) {
            content(nestedScrollConnection)
        }
    }
}
