package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Download
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EditArgumentScreen(
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    argId: Int = -1
) {
    val recentArguments by viewModel.recentArguments.collectAsState()
    val existingArg = remember(recentArguments, argId) { recentArguments.find { it.id == argId } }

    var antiMarxist by remember(existingArg) { mutableStateOf(existingArg?.antiMarxistStatement ?: "") }
    var marxist by remember(existingArg) { mutableStateOf(existingArg?.marxistCounterArgument ?: "") }
    var category by remember(existingArg) { mutableStateOf(existingArg?.category ?: "") }
    
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val jsonStr = context.contentResolver.openInputStream(it)?.use { inputStream ->
                    inputStream.bufferedReader().use { reader -> reader.readText() }
                }
                if (jsonStr != null) {
                    viewModel.importData(jsonStr)
                    navController.popBackStack()
                }
            }
        }
    }

    with(sharedTransitionScope) {
        PullToDismissContainer(
            onDismiss = { navController.popBackStack() }
        ) { nestedScrollConnection ->
        Scaffold(
            modifier = Modifier.fillMaxSize().let { m ->
                if (argId == -1) {
                    m.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "fab_neues_argument"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(0.dp)),
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                        boundsTransform = { _, _ -> androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 380f) }
                    )
                } else m
            },
            containerColor = ImmersiveBackground
        ) { padding ->
        Box(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalFadingEdge(topEdge = 20.dp, bottomEdge = 100.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(top = padding.calculateTopPadding() + 80.dp)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                OutlinedTextField(
                    value = category,
                onValueChange = { category = it },
                label = { Text("Kategorie (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ImmersiveTextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ImmersiveGreen,
                    focusedLabelColor = ImmersiveGreen,
                    unfocusedBorderColor = ImmersiveBorder,
                    focusedContainerColor = ImmersiveSurface,
                    unfocusedContainerColor = ImmersiveSurface,
                    focusedTextColor = ImmersiveTextPrimary,
                    unfocusedTextColor = ImmersiveTextPrimary
                )
            )

            OutlinedTextField(
                value = antiMarxist,
                onValueChange = { antiMarxist = it },
                label = { Text("Antimarxistisches Argument") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ImmersiveTextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ImmersiveGreen,
                    focusedLabelColor = ImmersiveGreen,
                    unfocusedBorderColor = ImmersiveBorder,
                    focusedContainerColor = ImmersiveSurface,
                    unfocusedContainerColor = ImmersiveSurface,
                    focusedTextColor = ImmersiveTextPrimary,
                    unfocusedTextColor = ImmersiveTextPrimary
                )
            )

            OutlinedTextField(
                value = marxist,
                onValueChange = { marxist = it },
                label = { Text("Marxistisches Gegenargument") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ImmersiveTextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ImmersiveGreen,
                    focusedLabelColor = ImmersiveGreen,
                    unfocusedBorderColor = ImmersiveBorder,
                    focusedContainerColor = ImmersiveSurface,
                    unfocusedContainerColor = ImmersiveSurface,
                    focusedTextColor = ImmersiveTextPrimary,
                    unfocusedTextColor = ImmersiveTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            val closeInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .bounceScale(closeInteractionSource)
                    .background(ImmersiveSurface, androidx.compose.foundation.shape.CircleShape),
                interactionSource = closeInteractionSource
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Abbrechen", tint = ImmersiveTextPrimary)
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ImmersiveSurface)
                    .border(1.dp, ImmersiveBorder, RoundedCornerShape(50))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (existingArg != null) "Argument bearbeiten" else "Neues Argument",
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
            }

            if (existingArg == null) {
                val importInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                IconButton(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier
                        .bounceScale(importInteractionSource)
                        .background(ImmersiveSurface, androidx.compose.foundation.shape.CircleShape),
                    interactionSource = importInteractionSource
                ) {
                    Icon(Icons.Filled.Download, contentDescription = "Importieren", tint = ImmersiveGreen)
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .padding(horizontal = 24.dp)
                .imePadding()
        ) {
            val btnInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            Button(
                onClick = {
                    if (antiMarxist.isNotBlank() && marxist.isNotBlank()) {
                        if (existingArg != null) {
                            viewModel.updateArgument(existingArg.copy(
                                antiMarxistStatement = antiMarxist,
                                marxistCounterArgument = marxist,
                                category = category
                            ))
                        } else {
                            viewModel.insertArgument(antiMarxist, marxist, category)
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(56.dp)
                    .bounceScale(btnInteractionSource)
                    .clip(androidx.compose.foundation.shape.CircleShape),
                interactionSource = btnInteractionSource,
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveGreen),
                enabled = antiMarxist.isNotBlank() && marxist.isNotBlank()
            ) {
                Text(
                    "Speichern",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveOnGreen,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
    }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EditGlossaryScreen(
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var term by remember { mutableStateOf("") }
    var definition by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val jsonStr = context.contentResolver.openInputStream(it)?.use { inputStream ->
                    inputStream.bufferedReader().use { reader -> reader.readText() }
                }
                if (jsonStr != null) {
                    viewModel.importData(jsonStr)
                    navController.popBackStack()
                }
            }
        }
    }

    with(sharedTransitionScope) {
        PullToDismissContainer(
            onDismiss = { navController.popBackStack() }
        ) { nestedScrollConnection ->
        Scaffold(
            modifier = Modifier.fillMaxSize().sharedBounds(
                sharedContentState = rememberSharedContentState(key = "fab_neuer_begriff"),
                animatedVisibilityScope = animatedVisibilityScope,
                enter = fadeIn(),
                exit = fadeOut(),
                clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(0.dp)),
                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                boundsTransform = { _, _ -> androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 380f) }
            ),
            containerColor = ImmersiveBackground
        ) { padding ->
        Box(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalFadingEdge(topEdge = 20.dp, bottomEdge = 100.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(top = padding.calculateTopPadding() + 80.dp)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                OutlinedTextField(
                    value = term,
                onValueChange = { term = it },
                label = { Text("Begriff") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ImmersiveGreen,
                    focusedLabelColor = ImmersiveGreen,
                    unfocusedBorderColor = ImmersiveBorder,
                    focusedContainerColor = ImmersiveSurface,
                    unfocusedContainerColor = ImmersiveSurface
                )
            )

            OutlinedTextField(
                value = definition,
                onValueChange = { definition = it },
                label = { Text("Definition") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 6,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ImmersiveGreen,
                    focusedLabelColor = ImmersiveGreen,
                    unfocusedBorderColor = ImmersiveBorder,
                    focusedContainerColor = ImmersiveSurface,
                    unfocusedContainerColor = ImmersiveSurface
                )
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            val closeInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .bounceScale(closeInteractionSource)
                    .background(ImmersiveSurface, androidx.compose.foundation.shape.CircleShape),
                interactionSource = closeInteractionSource
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Abbrechen", tint = ImmersiveTextPrimary)
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ImmersiveSurface)
                    .border(1.dp, ImmersiveBorder, RoundedCornerShape(50))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Neuer Begriff",
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
            }

            val importInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            IconButton(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier
                    .bounceScale(importInteractionSource)
                    .background(ImmersiveSurface, androidx.compose.foundation.shape.CircleShape),
                interactionSource = importInteractionSource
            ) {
                Icon(Icons.Filled.Download, contentDescription = "Importieren", tint = ImmersiveGreen)
            }
        }

        Box(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .padding(horizontal = 24.dp)
                .imePadding()
        ) {
            val btnInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            Button(
                onClick = {
                    if (term.isNotBlank() && definition.isNotBlank()) {
                        viewModel.insertGlossary(term, definition)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(56.dp)
                    .bounceScale(btnInteractionSource)
                    .clip(androidx.compose.foundation.shape.CircleShape),
                interactionSource = btnInteractionSource,
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveTextSecondary),
                enabled = term.isNotBlank() && definition.isNotBlank()
            ) {
                Text(
                    "Begriff speichern",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
    }
    }
}
}
