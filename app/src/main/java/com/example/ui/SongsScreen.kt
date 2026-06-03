package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.theme.*
import kotlinx.coroutines.delay

data class SongItem(val id: Int, val title: String, val artist: String, val durationMs: Long)

val communistSongs = listOf(
    SongItem(1, "Die Internationale", "Ernst Busch", 185000),
    SongItem(2, "Arbeiter von Wien", "Chor", 150000),
    SongItem(3, "Bandiera Rossa", "Various", 140000),
    SongItem(4, "Einheitsfrontlied", "Ernst Busch", 200000),
    SongItem(5, "Der Heimliche Aufmarsch", "Ernst Busch", 175000),
    SongItem(6, "Auferstanden aus Ruinen", "Chor", 160000),
    SongItem(7, "Du (Schade, dass Beton nicht brennt)", "Waving the Guns", 180000)
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SongsScreen(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var currentlyPlaying by remember { mutableStateOf<SongItem?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isPlaying, currentlyPlaying) {
        if (isPlaying && currentlyPlaying != null) {
            while (currentProgress < currentlyPlaying!!.durationMs) {
                delay(100L)
                currentProgress += 100L
            }
            if (currentProgress >= currentlyPlaying!!.durationMs) {
                isPlaying = false
                currentProgress = 0L
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
    ) {
        with(sharedTransitionScope) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "roter_faden_header"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(0.dp)),
                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                    )
                    .background(ImmersivePillBg)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BounceIconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.clip(CircleShape).background(ImmersivePillBg)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = ImmersiveTextPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Mediathek",
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "roter_faden_title"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                        ),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = ImmersiveGreen
                        )
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    itemsIndexed(communistSongs) { _, song ->
                        val isThisPlaying = currentlyPlaying == song
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isThisPlaying) ImmersiveSurface else Color.Transparent)
                                .bounceClick {
                                    if (currentlyPlaying == song) {
                                        isPlaying = !isPlaying
                                    } else {
                                        currentlyPlaying = song
                                        currentProgress = 0L
                                        isPlaying = true
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(CreamRed),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isThisPlaying && isPlaying) {
                                    Icon(Icons.Filled.Pause, contentDescription = "Pause", tint = ImmersiveGreen)
                                } else {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = ImmersiveGreen)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ImmersiveTextPrimary
                                    )
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = ImmersiveTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // Player bar at bottom
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                AnimatedVisibility(
                    visible = currentlyPlaying != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ImmersiveSurface)
                            .padding(WindowInsets.navigationBars.asPaddingValues())
                            .padding(16.dp)
                    ) {
                        currentlyPlaying?.let { song ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ImmersiveTextPrimary
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = ImmersiveTextSecondary
                                        )
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { 
                                            val idx = communistSongs.indexOf(song)
                                            if (idx > 0) {
                                                currentlyPlaying = communistSongs[idx - 1]
                                                currentProgress = 0L
                                                isPlaying = true
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = ImmersiveTextPrimary)
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(CreamRed)
                                            .bounceClick { isPlaying = !isPlaying },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, 
                                            contentDescription = "Play/Pause", 
                                            tint = ImmersiveGreen,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { 
                                            val idx = communistSongs.indexOf(song)
                                            if (idx < communistSongs.size - 1) {
                                                currentlyPlaying = communistSongs[idx + 1]
                                                currentProgress = 0L
                                                isPlaying = true
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = ImmersiveTextPrimary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            // Progress bar
                            val progressFloat = (currentProgress.toFloat() / song.durationMs.toFloat()).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progressFloat },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = ImmersiveGreen,
                                trackColor = ImmersiveBorder
                            )
                        }
                    }
                }
            }
        }
    }
}
