package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val isCheckingForUpdates by viewModel.isCheckingForUpdates.collectAsState()
    val updateCheckResult by viewModel.updateCheckResult.collectAsState()
    val githubRepoPath by viewModel.githubRepoPath.collectAsState()
    var editRepoPath by remember(githubRepoPath) { mutableStateOf(githubRepoPath) }
    var showDialog by remember { mutableStateOf(false) }
    val updateInfo by viewModel.updateInfo.collectAsState()
    val updateDownloadProgress by viewModel.updateDownloadProgress.collectAsState()
    val customMarxImagePath by viewModel.customMarxImagePath.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.saveCustomMarxImage(context, uri)
        }
    }

    Scaffold(
        containerColor = ImmersiveBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .background(ImmersiveSurface, shape = RoundedCornerShape(12.dp))
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = ImmersiveGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Einstellungen",
                    color = ImmersiveTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Title
            Text(
                text = "Allgemein",
                color = ImmersiveTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Tutorial Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        viewModel.resetOnboarding()
                        navController.navigate("onboarding") {
                            popUpTo("main") { inclusive = false }
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(ImmersiveGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Tutorial",
                            tint = ImmersiveGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tutorial erneut öffnen",
                            color = ImmersiveTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Starte das Onboarding, um die App-Funktionen kennenzulernen.",
                            color = ImmersiveTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Section Title: Karl Marx Button Customizer
            Text(
                text = "Marx-Button Personalisierung",
                color = ImmersiveTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Display either custom image preview or default placeholder
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(2.dp, CircleShape)
                                .background(ImmersiveBackground, CircleShape)
                                .border(2.dp, ImmersiveGreen, CircleShape)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (customMarxImagePath != null) {
                                AsyncImage(
                                    model = customMarxImagePath,
                                    contentDescription = "Vorschau",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Standard-Icon",
                                    tint = ImmersiveTextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Eigenes Bild für Marx-Button",
                                color = ImmersiveTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (customMarxImagePath != null) {
                                    "Dein eigenes Bild wird als drehender Button und im Marx-Chat verwendet!"
                                } else {
                                    "Ersetze die Standardzeichnung durch ein eigenes Foto von Karl Marx oder ein lustiges Meme-Bild."
                                },
                                color = ImmersiveTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = ImmersiveGreen),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (customMarxImagePath != null) "Bild ändern" else "Bild hochladen",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (customMarxImagePath != null) {
                            OutlinedButton(
                                onClick = { viewModel.removeCustomMarxImage(context) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CreamRed),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CreamRed.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Entfernen",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section Title: Updates
            Text(
                text = "Updates & Entwickler",
                color = ImmersiveTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            // GitHub Repostory Config Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(ImmersiveGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Repository",
                                tint = ImmersiveGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GitHub-Repository",
                                color = ImmersiveTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Repository-Pfad für APK-Updates.",
                                color = ImmersiveTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = editRepoPath,
                        onValueChange = {
                            editRepoPath = it
                            viewModel.updateGithubRepoPath(it)
                        },
                        placeholder = { Text("owner/repo", color = ImmersiveTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ImmersiveTextPrimary,
                            unfocusedTextColor = ImmersiveTextPrimary,
                            focusedBorderColor = ImmersiveGreen,
                            unfocusedBorderColor = ImmersiveBorder,
                            focusedContainerColor = ImmersiveBackground,
                            unfocusedContainerColor = ImmersiveBackground
                        )
                    )
                }
            }

            // Update Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        viewModel.checkForUpdates(context, isForceCheck = false, isManual = true)
                        showDialog = true
                    },
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(ImmersiveGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Anwendungs-Updates",
                            tint = ImmersiveGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auf Updates prüfen",
                            color = ImmersiveTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Prüft auf neue Aktualisierungen der App.",
                            color = ImmersiveTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // App Version Info
            Text(
                text = "Roter Faden • Beta 1.58\nEntwickelt von Agronalin",
                color = ImmersiveTextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                lineHeight = 16.sp
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!isCheckingForUpdates && updateDownloadProgress == null) showDialog = false },
            containerColor = ImmersiveSurface,
            titleContentColor = ImmersiveTextPrimary,
            textContentColor = ImmersiveTextSecondary,
            title = {
                Text(
                    text = if (isCheckingForUpdates) "Suche läuft..." else "Update-Prüfung",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isCheckingForUpdates) {
                        CircularProgressIndicator(
                            color = ImmersiveGreen,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Der GitHub-Server wird nach Releases abgefragt...",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        val resultText = updateCheckResult ?: ""
                        val isUpdateAvailable = updateInfo != null
                        val isSuccess = resultText.contains("neuesten Stand") || isUpdateAvailable
                        
                        Icon(
                            imageVector = if (isUpdateAvailable) Icons.Default.CloudDownload else if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = "Status",
                            tint = if (isSuccess) ImmersiveGreen else Color(0xFFEF5350),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = resultText,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        )

                        if (isUpdateAvailable && updateInfo != null) {
                            val info = updateInfo!!
                            if (info.changelog.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Änderungen in v${info.versionName}:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ImmersiveTextSecondary,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ImmersiveBackground, RoundedCornerShape(8.dp))
                                        .border(1.dp, ImmersiveBorder, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = info.changelog,
                                        fontSize = 11.sp,
                                        color = ImmersiveTextPrimary
                                    )
                                }
                            }

                            if (updateDownloadProgress != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = { updateDownloadProgress ?: 0f },
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                                        color = ImmersiveGreen,
                                        trackColor = ImmersiveBorder
                                    )
                                    val percent = ((updateDownloadProgress ?: 0f) * 100).toInt()
                                    Text(
                                        text = "Wird heruntergeladen... $percent%",
                                        color = ImmersiveTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (!isCheckingForUpdates) {
                    val info = updateInfo
                    if (info != null) {
                        Button(
                            onClick = {
                                viewModel.downloadAndInstallUpdate(context, info.downloadUrl)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ImmersiveGreen),
                            enabled = updateDownloadProgress == null,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Jetzt installieren", color = Color.White)
                        }
                    } else {
                        TextButton(
                            onClick = { showDialog = false }
                        ) {
                            Text("Schließen", color = ImmersiveGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            dismissButton = {
                if (!isCheckingForUpdates && updateInfo != null) {
                    TextButton(
                        onClick = { showDialog = false },
                        enabled = updateDownloadProgress == null
                    ) {
                        Text("Später", color = ImmersiveTextSecondary)
                    }
                }
            }
        )
    }
}
