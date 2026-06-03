package com.example.ui

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
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
import com.example.data.SongEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SongsScreen(
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current
    val songs by viewModel.allSongs.collectAsState()

    var currentlyPlaying by remember { mutableStateOf<SongEntity?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(1) }

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                var title = "Unknown Song"
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            title = cursor.getString(nameIndex)
                        }
                    }
                }
                viewModel.insertSong(SongEntity(it.toString(), title, "Lied"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    DisposableEffect(currentlyPlaying) {
        mediaPlayer?.release()
        val mp = MediaPlayer()
        if (currentlyPlaying != null) {
            try {
                mp.setDataSource(context, Uri.parse(currentlyPlaying!!.uriString))
                mp.prepareAsync()
                mp.setOnPreparedListener {
                    duration = it.duration
                    it.start()
                    isPlaying = true
                }
                mp.setOnCompletionListener {
                    isPlaying = false
                    currentProgress = duration
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaPlayer = mp

        onDispose {
            mp.release()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentProgress = mediaPlayer?.currentPosition ?: 0
            delay(100L)
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
                    .background(ImmersivePillBg)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "roter_faden_header"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(50)),
                                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            ).weight(1f),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = ImmersiveGreen
                            )
                        )
                        BounceIconButton(
                            onClick = { filePickerLauncher.launch(arrayOf("audio/*")) },
                            modifier = Modifier.clip(CircleShape).background(ImmersiveGreen)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Hinzufügen", tint = ImmersiveOnGreen)
                        }
                    }
                }

                if (songs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 120.dp), contentAlignment = Alignment.Center) {
                        Text("Noch keine Lieder hinzugefügt.", color = ImmersiveTextSecondary)
                    }
                } else {
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
                                            if (isPlaying) mediaPlayer?.pause() else mediaPlayer?.start()
                                            isPlaying = !isPlaying
                                        } else {
                                            currentlyPlaying = song
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
                                IconButton(onClick = { viewModel.deleteSong(song) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Löschen", tint = Color.Red.copy(alpha = 0.5f))
                                }
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
                                            .bounceClick {
                                                if (isPlaying) {
                                                    mediaPlayer?.pause()
                                                    isPlaying = false
                                                } else {
                                                    mediaPlayer?.start()
                                                    isPlaying = true
                                                }
                                            },
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
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = ImmersiveTextPrimary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            // Progress bar / Slider
                            Slider(
                                value = currentProgress.toFloat(),
                                onValueChange = { newVal ->
                                    currentProgress = newVal.toInt()
                                    mediaPlayer?.seekTo(newVal.toInt())
                                },
                                valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
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
