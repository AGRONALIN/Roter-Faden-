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
    val dummyConnection = remember {
        object : NestedScrollConnection {}
    }
    Box(modifier = modifier.fillMaxSize()) {
        content(dummyConnection)
    }
}
