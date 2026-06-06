package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import com.example.data.Argument
import com.example.ui.theme.*
import androidx.compose.material.icons.filled.Upload
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.animateContentSize

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ArgumentDetailScreen(
    argument: Argument,
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sourceKey: String = "card"
) {
    val glossaryItems by viewModel.glossaryItems.collectAsState()
    val literatureItems by viewModel.literatureList.collectAsState()
    val tooltipState by viewModel.tooltipState.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val literatureColor = if (isDarkTheme) Color(0xFF66BB6A) else Color(0xFF2E7D32)

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var containerPosition by remember { mutableStateOf(Offset.Zero) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    var showExportSelectionDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val density = androidx.compose.ui.platform.LocalDensity.current
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val jsonStr = viewModel.exportSingleArgument(argument.id)
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
                    text = "Argumentation teilen / speichern",
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
                        text = "Wähle eine Methode, um diese Argumentation zu übertragen:",
                        color = ImmersiveTextSecondary,
                        fontSize = 14.sp
                    )
                    
                    Button(
                        onClick = {
                            showExportSelectionDialog = false
                            scope.launch {
                                val jsonStr = viewModel.exportSingleArgument(argument.id)
                                ShareHelper.shareTextAsFile(context, jsonStr, "Roter Faden - Argumentation")
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
                            val fileName = "debattenbank_argument_${argument.id}_${dateFormat.format(Date())}.json"
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
    
    val blurRadius by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (tooltipState.isVisible) 12.dp else 0.dp,
        animationSpec = tween(300),
        label = "blur"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
    ) {
        PullToDismissContainer(
            onDismiss = { navController.popBackStack() }
        ) { nestedScrollConnection ->
            with(sharedTransitionScope) {
                Column(
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "argument_${argument.id}_${sourceKey}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            enter = fadeIn(animationSpec = tween(200)),
                            exit = fadeOut(animationSpec = tween(150)),
                            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(12.dp)),
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
                        .background(ImmersiveBackground)
                        .padding(horizontal = 24.dp)
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
                        .verticalFadingEdge(topEdge = 20.dp, bottomEdge = 40.dp)
                        .verticalScroll(rememberScrollState())
                        .onGloballyPositioned { coordinates ->
                            containerPosition = coordinates.positionInWindow()
                        }
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null, // No ripple when clicking background
                            onClick = { viewModel.hideTooltip() }
                        )
                ) {
                // Header (Back button & Delete)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BounceIconButton(
                        onClick = { navController.popBackStack() },
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
                                    navController.navigate("edit_argument/${argument.id}")
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
                            onClick = { showOptions = !showOptions }
                        ) {
                            Text("★", color = Color.Red, fontSize = 28.sp)
                        }
                    }
                }

                argument.imagePath?.let { path ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, ImmersiveBorder, RoundedCornerShape(16.dp))
                    ) {
                        LocalImageFromPath(
                            path = path,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (argument.category.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(ImmersivePillBg)
                            .border(1.dp, ImmersiveBorder, RoundedCornerShape(percent = 50))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = argument.category.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                // 1. Anti-Marxist Statement Bubble
                val immersiveGreenColor = ImmersiveGreen

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CreamBlue, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ARGUMENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ImmersiveTextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    RichTextWithImages(
                        text = argument.antiMarxistStatement,
                        glossaryItems = glossaryItems,
                        literatureItems = literatureItems,
                        highlightColor = immersiveGreenColor,
                        literatureColor = literatureColor,
                        navController = navController,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary,
                            lineHeight = 28.sp,
                            letterSpacing = (-0.25).sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Marxist Counter Argument Bubble
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CreamRed, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "GEGENARGUMENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ImmersiveTextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    RichTextWithImages(
                        text = argument.marxistCounterArgument,
                        glossaryItems = glossaryItems,
                        literatureItems = literatureItems,
                        highlightColor = immersiveGreenColor,
                        literatureColor = literatureColor,
                        navController = navController,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                            color = ImmersiveTextPrimary,
                            lineHeight = 32.sp,
                            fontSize = 22.sp
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(120.dp))
            }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Löschen bestätigen",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = ImmersiveTextPrimary)
                )
            },
            text = {
                Text(
                    text = "Möchtest du dieses Argument wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = ImmersiveTextSecondary)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteArgument(argument)
                        navController.popBackStack()
                    }
                ) {
                    Text("Löschen", color = ImmersiveGreen)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Abbrechen", color = ImmersiveTextSecondary)
                }
            },
            containerColor = ImmersiveSurface,
            titleContentColor = ImmersiveTextPrimary,
            textContentColor = ImmersiveTextSecondary
        )
    }
}
