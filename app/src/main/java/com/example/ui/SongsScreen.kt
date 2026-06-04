package com.example.ui

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.UUID

data class SongItem(val id: String, val title: String, val artist: String, val durationMs: Long, val uri: Uri)

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SongsScreen(
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current
    val savedSongs by viewModel.songsList.collectAsState()
    val songs = remember(savedSongs) {
        savedSongs.map {
            SongItem(
                id = it.id,
                title = it.title,
                artist = it.artist,
                durationMs = it.durationMs,
                uri = Uri.parse(it.uriString)
            )
        }
    }
    var currentlyPlaying by remember { mutableStateOf<SongItem?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableIntStateOf(0) }
    var isSeeking by remember { mutableStateOf(false) }

    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    LaunchedEffect(currentlyPlaying) {
        if (currentlyPlaying != null) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(context, currentlyPlaying!!.uri)
                mediaPlayer.prepare()
                mediaPlayer.start()
                isPlaying = true
            } catch (e: Exception) {
                e.printStackTrace()
                isPlaying = false
            }
        } else {
            mediaPlayer.reset()
            isPlaying = false
        }
    }

    LaunchedEffect(isPlaying, isSeeking) {
        if (isPlaying && !isSeeking) {
            try {
                mediaPlayer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            while (isPlaying && !isSeeking) {
                delay(200L)
                try {
                    if (mediaPlayer.isPlaying) {
                        currentProgress = mediaPlayer.currentPosition
                    }
                } catch (e: Exception) {
                    // Ignore illegal state
                }
            }
        } else if (!isPlaying && !isSeeking) {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    DisposableEffect(mediaPlayer) {
        mediaPlayer.setOnCompletionListener {
            isPlaying = false
            currentProgress = mediaPlayer.duration
        }
        onDispose {
            mediaPlayer.setOnCompletionListener(null)
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Unbekannter Titel"
                val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unbekannter Künstler"
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = durationStr?.toLongOrNull() ?: 0L
                retriever.release()
                
                val songId = UUID.randomUUID().toString()
                
                // Copy MP3 file to application internal directory for persistent local play
                val inputStream = context.contentResolver.openInputStream(uri)
                val destFile = java.io.File(context.filesDir, "$songId.mp3")
                inputStream?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                viewModel.insertSong(
                    id = songId,
                    title = title,
                    artist = artist,
                    durationMs = duration,
                    uriString = Uri.fromFile(destFile).toString()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        with(sharedTransitionScope) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ImmersiveBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp,
                            start = 12.dp, 
                            end = 24.dp,
                            bottom = 16.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BounceIconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = ImmersiveTextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mediathek",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            color = ImmersiveGreen
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    BounceIconButton(
                        onClick = { launcher.launch("audio/*") },
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(ImmersiveGreen)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Lied hinzufügen", tint = Color.White)
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    itemsIndexed(songs) { _, song ->
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
                                        currentProgress = 0
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
                                val displayTitle = if (song.artist.isNotEmpty() && song.artist != "Unbekannter Künstler") "${song.artist} - ${song.title}" else song.title
                                Text(
                                    text = displayTitle,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ImmersiveTextPrimary
                                    )
                                )
                                Text(
                                    text = "Lied",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = ImmersiveTextSecondary
                                    )
                                )
                            }
                            IconButton(onClick = {
                                if (currentlyPlaying == song) {
                                    currentlyPlaying = null
                                    isPlaying = false
                                }
                                try {
                                    val uriStr = song.uri.toString()
                                    if (uriStr.startsWith("file://")) {
                                        val path = song.uri.path
                                        if (path != null) {
                                            java.io.File(path).delete()
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                viewModel.deleteSong(song.id)
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Löschen", tint = ImmersiveGreen.copy(alpha = 0.6f))
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
                                            val idx = songs.indexOf(song)
                                            if (idx > 0) {
                                                currentlyPlaying = songs[idx - 1]
                                                currentProgress = 0
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
                                            val idx = songs.indexOf(song)
                                            if (idx < songs.size - 1) {
                                                currentlyPlaying = songs[idx + 1]
                                                currentProgress = 0
                                                isPlaying = true
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = ImmersiveTextPrimary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            // Progress bar slider
                            val durationFloat = if (song.durationMs > 0) song.durationMs.toFloat() else 1f
                            val progressFloat = (currentProgress.toFloat() / durationFloat).coerceIn(0f, 1f)
                            
                            Slider(
                                value = progressFloat,
                                onValueChange = { newValue ->
                                    isSeeking = true
                                    currentProgress = (newValue * durationFloat).toInt()
                                },
                                onValueChangeFinished = {
                                    isSeeking = false
                                    try {
                                        mediaPlayer.seekTo(currentProgress)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(24.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = ImmersiveGreen,
                                    activeTrackColor = ImmersiveGreen,
                                    inactiveTrackColor = ImmersiveBorder
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
