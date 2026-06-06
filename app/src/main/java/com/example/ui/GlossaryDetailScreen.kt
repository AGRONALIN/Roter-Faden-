package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavController
import com.example.data.GlossaryItem
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun GlossaryDetailScreen(
    glossaryItem: GlossaryItem,
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sourceKey: String = "card"
) {
    var showOptions by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportSelectionDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val jsonStr = viewModel.exportSingleGlossary(glossaryItem.id)
                context.contentResolver.openOutputStream(it)?.use { out ->
                    out.write(jsonStr.toByteArray())
                }
            }
        }
    }

    if (showExportSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showExportSelectionDialog = false },
            containerColor = ImmersiveBackground,
            title = {
                Text(
                    text = "Glossar teilen / speichern",
                    color = ImmersiveTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Wähle eine Methode, um diesen Begriff zu übertragen:",
                        color = ImmersiveTextSecondary,
                        fontSize = 14.sp
                    )
                    
                    Button(
                        onClick = {
                            showExportSelectionDialog = false
                            scope.launch {
                                val jsonStr = viewModel.exportSingleGlossary(glossaryItem.id)
                                ShareHelper.shareTextAsFile(context, jsonStr, "Roter Faden - Glossar")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ImmersiveGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Text("Drahtlos teilen (WhatsApp / Quick Share)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            showExportSelectionDialog = false
                            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                            val fileName = "debattenbank_glossary_${glossaryItem.id}_${dateFormat.format(Date())}.json"
                            exportLauncher.launch(fileName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CreamRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Text("Als JSON-Datei speichern", color = ImmersiveGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportSelectionDialog = false }) {
                    Text("Abbrechen", color = ImmersiveTextSecondary)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Begriff löschen?") },
            text = { Text("Möchtest du diesen Eintrag wirklich löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteGlossary(glossaryItem)
                    navController.popBackStack()
                }) {
                    Text("Löschen", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Abbrechen", color = ImmersiveTextPrimary)
                }
            },
            containerColor = ImmersiveSurface,
            titleContentColor = ImmersiveTextPrimary,
            textContentColor = ImmersiveTextSecondary
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
    ) {
        val allArguments by viewModel.recentArguments.collectAsState()
        val glossaryItems by viewModel.glossaryItems.collectAsState()
        val literatureItems by viewModel.literatureList.collectAsState()
        val isDarkTheme by viewModel.isDarkTheme.collectAsState()
        val literatureColor = if (isDarkTheme) Color(0xFF66BB6A) else Color(0xFF2E7D32)
        val relatedArguments = remember(allArguments, glossaryItem.term) {
            allArguments.filter {
                it.marxistCounterArgument.contains(glossaryItem.term, ignoreCase = true) ||
                it.antiMarxistStatement.contains(glossaryItem.term, ignoreCase = true)
            }
        }
        
    PullToDismissContainer(
        onDismiss = {
            navController.popBackStack()
        }
    ) { nestedScrollConnection ->
        with(sharedTransitionScope) {
            Column(
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "glossary_${glossaryItem.id}_${sourceKey}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        enter = fadeIn(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(150)),
                        clipInOverlayDuringTransition = OverlayClip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(contentScale = androidx.compose.ui.layout.ContentScale.Crop),
                        boundsTransform = { _, _ ->
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        }
                    )
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
                    .background(ImmersivePillBg)
                    .padding(horizontal = 24.dp)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
                    .verticalFadingEdge(topEdge = 20.dp, bottomEdge = 40.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header (Back button & Delete)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BounceIconButton(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier.clip(CircleShape).background(ImmersivePillBg)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = ImmersiveTextPrimary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedVisibility(
                            visible = showOptions,
                            enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
                            exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .background(CreamRed, CircleShape)
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                BounceIconButton(onClick = {
                                    showOptions = false
                                    navController.navigate("edit_glossary/${glossaryItem.id}")
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Bearbeiten", tint = ImmersiveTextPrimary)
                                }
                                BounceIconButton(onClick = {
                                    showOptions = false
                                    showExportSelectionDialog = true
                                }) {
                                    Icon(Icons.Filled.Upload, contentDescription = "Exportieren", tint = ImmersiveGreen)
                                }
                                BounceIconButton(onClick = {
                                    showOptions = false
                                    showDeleteDialog = true
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Löschen", tint = ImmersiveTextSecondary)
                                }
                            }
                        }

                        BounceIconButton(
                            onClick = { showOptions = !showOptions },
                        ) {
                            Text("★", color = Color.Red, fontSize = 28.sp)
                        }
                    }
                }
                
                Text(
                    text = glossaryItem.term, 
                    fontWeight = FontWeight.Black,
                    color = ImmersiveGreen,
                    style = MaterialTheme.typography.headlineMedium
                ) 
                
                Spacer(modifier = Modifier.height(16.dp))

                glossaryItem.imagePath?.let { path ->
                    TransformableLocalImage(
                        path = path,
                        transformKey = "glossary_${glossaryItem.id}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
            val immersiveGreenColor = ImmersiveGreen

            RichTextWithImages(
                text = glossaryItem.definition,
                glossaryItems = glossaryItems.filter { it.id != glossaryItem.id },
                literatureItems = literatureItems,
                highlightColor = immersiveGreenColor,
                literatureColor = literatureColor,
                navController = navController,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    color = ImmersiveTextSecondary,
                    lineHeight = 28.sp,
                    fontSize = 18.sp
                )
            )
            
            if (relatedArguments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Red.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kommt vor in:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary
                    )
                )
                
                relatedArguments.forEach { argument ->
                    Card(
                        modifier = Modifier.fillMaxWidth().bounceClick {
                            navController.navigate("argument_detail/${argument.id}")
                        },
                        colors = CardDefaults.cardColors(containerColor = ImmersivePillBg),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = argument.antiMarxistStatement,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveTextPrimary
                                ),
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = argument.marxistCounterArgument,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = ImmersiveTextSecondary
                                ),
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
                Spacer(modifier = Modifier.height(120.dp))
            }
            }
        }
    }
}
