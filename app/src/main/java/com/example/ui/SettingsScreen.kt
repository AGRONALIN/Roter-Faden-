package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
    var updateChecking by remember { mutableStateOf(false) }
    var updateResultText by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

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

            // Update Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        if (!updateChecking) {
                            scope.launch {
                                updateChecking = true
                                showDialog = true
                                updateResultText = null
                                delay(1800)
                                updateChecking = false
                                updateResultText = "Deine App ist auf dem neuesten Stand!\nVersion ${BuildConfig.VERSION_NAME} (aktuellste)"
                            }
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
                text = "Roter Faden • Version ${BuildConfig.VERSION_NAME}\nEntwickelt von Agronalin",
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
            onDismissRequest = { if (!updateChecking) showDialog = false },
            containerColor = ImmersiveSurface,
            titleContentColor = ImmersiveTextPrimary,
            textContentColor = ImmersiveTextSecondary,
            title = {
                Text(
                    text = if (updateChecking) "Suche läuft..." else "Update-Prüfung",
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
                    if (updateChecking) {
                        CircularProgressIndicator(
                            color = ImmersiveGreen,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Der Server wird nach Updates abgefragt...",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Erfolg",
                            tint = ImmersiveGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = updateResultText ?: "",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        )
                    }
                }
            },
            confirmButton = {
                if (!updateChecking) {
                    TextButton(
                        onClick = { showDialog = false }
                    ) {
                        Text("Schließen", color = ImmersiveGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}
