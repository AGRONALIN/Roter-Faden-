package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Smartphone
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
import androidx.navigation.NavController
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CollectionScreen(
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navAnimatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Argumente", "Glossar", "Literatur")

    val allArguments by viewModel.recentArguments.collectAsState()
    val allGlossaryItems by viewModel.glossaryItems.collectAsState()
    val allLiteratureItems by viewModel.literatureList.collectAsState()

    var selectedArgumentIds by remember { mutableStateOf(setOf<Int>()) }
    var selectedGlossaryIds by remember { mutableStateOf(setOf<Int>()) }
    var selectedLiteratureIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedArgumentIds.isNotEmpty() || selectedGlossaryIds.isNotEmpty() || selectedLiteratureIds.isNotEmpty()

    var showDeleteDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = isSelectionMode) {
        selectedArgumentIds = emptySet()
        selectedGlossaryIds = emptySet()
        selectedLiteratureIds = emptySet()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState0 = androidx.compose.foundation.lazy.rememberLazyListState()
    val listState1 = androidx.compose.foundation.lazy.rememberLazyListState()
    val listState2 = androidx.compose.foundation.lazy.rememberLazyListState()

    val activeListState = remember(selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> listState0
            1 -> listState1
            else -> listState2
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.setBottomBarVisible(true)
        }
    }

    LaunchedEffect(activeListState) {
        var previousIndex = activeListState.firstVisibleItemIndex
        var previousScrollOffset = activeListState.firstVisibleItemScrollOffset
        androidx.compose.runtime.snapshotFlow { activeListState.firstVisibleItemIndex to activeListState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (index > previousIndex || (index == previousIndex && offset > previousScrollOffset + 10)) {
                    viewModel.setBottomBarVisible(false)
                } else if (index < previousIndex || (index == previousIndex && offset < previousScrollOffset - 10)) {
                    viewModel.setBottomBarVisible(true)
                }
                previousIndex = index
                previousScrollOffset = offset
            }
    }

    var showExportSelectionDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val jsonStr = viewModel.exportSelectedItems(selectedArgumentIds, selectedGlossaryIds, selectedLiteratureIds)
                context.contentResolver.openOutputStream(it)?.use { out ->
                    out.write(jsonStr.toByteArray())
                }
                selectedArgumentIds = emptySet()
                selectedGlossaryIds = emptySet()
                selectedLiteratureIds = emptySet()
            }
        }
    }

    val generalImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { selectedUri ->
            scope.launch {
                val jsonStr = context.contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                    inputStream.bufferedReader().use { reader -> reader.readText() }
                }
                if (jsonStr != null) {
                    val success = viewModel.importData(jsonStr)
                    if (success) {
                        android.widget.Toast.makeText(context, "Erfolgreich importiert!", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Fehler beim Importieren!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    if (showExportSelectionDialog) {
        val totalSelectedCount = selectedArgumentIds.size + selectedGlossaryIds.size + selectedLiteratureIds.size
        AlertDialog(
            onDismissRequest = { showExportSelectionDialog = false },
            containerColor = ImmersiveBackground,
            title = {
                Text(
                    text = "Inhalt teilen / speichern",
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
                        text = "Wähle eine Methode, um deine $totalSelectedCount ausgewählten Einträge zu übertragen:",
                        color = ImmersiveTextSecondary,
                        fontSize = 14.sp
                    )
                    
                    Button(
                        onClick = {
                            showExportSelectionDialog = false
                            scope.launch {
                                val jsonStr = viewModel.exportSelectedItems(selectedArgumentIds, selectedGlossaryIds, selectedLiteratureIds)
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, jsonStr)
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Roter Faden - Export")
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Inhalte teilen"))
                                selectedArgumentIds = emptySet()
                                selectedGlossaryIds = emptySet()
                                selectedLiteratureIds = emptySet()
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
                            val fileName = "debattenbank_export_${dateFormat.format(Date())}.json"
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

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = ImmersiveBackground,
            title = {
                Text(
                    text = "Daten importieren",
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
                        text = "Füge neue Argumentationen oder Glossareinträge über eine JSON-Datei oder aus deiner Zwischenablage hinzu:",
                        color = ImmersiveTextSecondary,
                        fontSize = 14.sp
                    )
                    
                    Button(
                        onClick = {
                            showImportDialog = false
                            generalImportLauncher.launch(arrayOf("application/json"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CreamRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Text("Aus einer JSON-Datei laden", color = ImmersiveGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            showImportDialog = false
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clipData = clipboard.primaryClip
                            if (clipData != null && clipData.itemCount > 0) {
                                val text = clipData.getItemAt(0).text?.toString() ?: ""
                                if (text.isNotBlank()) {
                                    scope.launch {
                                        val success = viewModel.importData(text)
                                        if (success) {
                                            android.widget.Toast.makeText(context, "Erfolgreich aus Zwischenablage importiert!", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Fehler beim Importieren! Überprüfe die Daten.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "Die Zwischenablage ist leer!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                android.widget.Toast.makeText(context, "Die Zwischenablage ist leer!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ImmersiveGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Text("Aus der Zwischenablage einfügen", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Abbrechen", color = ImmersiveTextSecondary)
                }
            }
        )
    }

    if (showDeleteDialog) {
        val totalSelectedCount = selectedArgumentIds.size + selectedGlossaryIds.size + selectedLiteratureIds.size
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Wirklich löschen?") },
            text = { Text("Möchtest du diese $totalSelectedCount Einträge endgültig löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    if (selectedArgumentIds.isNotEmpty()) {
                        viewModel.deleteArgumentsById(selectedArgumentIds)
                    }
                    if (selectedGlossaryIds.isNotEmpty()) {
                        viewModel.deleteGlossariesById(selectedGlossaryIds)
                    }
                    if (selectedLiteratureIds.isNotEmpty()) {
                        viewModel.deleteLiteratureById(selectedLiteratureIds)
                    }
                    selectedArgumentIds = emptySet()
                    selectedGlossaryIds = emptySet()
                    selectedLiteratureIds = emptySet()
                    showDeleteDialog = false
                }) {
                    Text("Löschen", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Abbrechen", color = ImmersiveTextPrimary)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp))
                
                // Top header - Title only (Import button removed)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alle Einträge", // Like Library
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = ImmersiveTextPrimary,
                            letterSpacing = (-1).sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tabs / Chips
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(if (isSelected) ImmersiveGreen else CreamRed)
                                .bounceClick { selectedTabIndex = index }
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = if (isSelected) Color.White else ImmersiveGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = isSelectionMode) {
                    val totalSelectedItems = selectedArgumentIds.size + selectedGlossaryIds.size + selectedLiteratureIds.size
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left action button (Export)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(ImmersiveGreen)
                                .bounceClick {
                                    showExportSelectionDialog = true
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Upload, contentDescription = "Export", modifier = Modifier.size(20.dp), tint = ImmersiveOnGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export ($totalSelectedItems)", fontWeight = FontWeight.Bold, color = ImmersiveOnGreen)
                            }
                        }
                        
                        // Right icons (Delete / Close)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Red.copy(alpha = 0.2f))
                                    .bounceClick { showDeleteDialog = true }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", modifier = Modifier.size(24.dp), tint = Color.Red)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ImmersiveDarkNav)
                                    .bounceClick { 
                                        selectedArgumentIds = emptySet()
                                        selectedGlossaryIds = emptySet()
                                        selectedLiteratureIds = emptySet()
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Cancel", modifier = Modifier.size(24.dp), tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(
                    modifier = Modifier.fillMaxSize().weight(1f).padding(horizontal = 24.dp).verticalFadingEdge(topEdge = 20.dp, bottomEdge = 40.dp),
                    targetState = selectedTabIndex,
                    transitionSpec = {
                        val springSpec = androidx.compose.animation.core.spring<androidx.compose.ui.unit.IntOffset>(
                            dampingRatio = 0.8f,
                            stiffness = 50f
                        )
                        val fadeSpec = tween<Float>(800)

                        if (targetState > initialState) {
                            (slideInHorizontally(
                                animationSpec = springSpec,
                                initialOffsetX = { fullWidth -> fullWidth }
                            ) + fadeIn(animationSpec = fadeSpec)).togetherWith(
                                slideOutHorizontally(
                                    animationSpec = springSpec,
                                    targetOffsetX = { fullWidth -> -fullWidth }
                                ) + fadeOut(animationSpec = fadeSpec)
                            )
                        } else {
                            (slideInHorizontally(
                                animationSpec = springSpec,
                                initialOffsetX = { fullWidth -> -fullWidth }
                            ) + fadeIn(animationSpec = fadeSpec)).togetherWith(
                                slideOutHorizontally(
                                    animationSpec = springSpec,
                                    targetOffsetX = { fullWidth -> fullWidth }
                                ) + fadeOut(animationSpec = fadeSpec)
                            )
                        }
                    },
                    label = "tab animation"
                ) { targetIndex ->
                    when (targetIndex) {
                        0 -> {
                            LazyColumn(
                                state = listState0,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (allArguments.isEmpty()) {
                                    item {
                                        EmptyStateMessage("Noch keine Argumente vorhanden.")
                                    }
                                } else {
                                    itemsIndexed(
                                        items = allArguments,
                                        key = { _, argument -> argument.id }
                                    ) { index, argument ->
                                        Column {
                                            val isSelected = selectedArgumentIds.contains(argument.id)
                                            ArgumentCard(
                                                argument = argument,
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        selectedArgumentIds = if (isSelected) {
                                                            selectedArgumentIds - argument.id
                                                        } else {
                                                            selectedArgumentIds + argument.id
                                                        }
                                                    } else {
                                                        viewModel.updateArgumentLastAccessed(argument)
                                                        navController.navigate("argument_detail/${argument.id}?source=card")
                                                    }
                                                },
                                                onLongClick = {
                                                    selectedArgumentIds = if (isSelected) {
                                                        selectedArgumentIds - argument.id
                                                    } else {
                                                        selectedArgumentIds + argument.id
                                                    }
                                                },
                                                isSelected = isSelected,
                                                sharedTransitionScope = sharedTransitionScope,
                                                animatedVisibilityScope = animatedVisibilityScope,
                                                navAnimatedVisibilityScope = navAnimatedVisibilityScope,
                                                sourceKey = "card"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            LazyColumn(
                                state = listState1,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 120.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (allGlossaryItems.isEmpty()) {
                                    item {
                                        EmptyStateMessage("Noch keine Begriffe vorhanden.")
                                    }
                                } else {
                                    itemsIndexed(
                                        items = allGlossaryItems,
                                        key = { _, item -> "gloss_${item.id}" }
                                    ) { index, item ->
                                        Column {
                                            val isSelected = selectedGlossaryIds.contains(item.id)
                                            GlossaryCard(
                                                item = item,
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        selectedGlossaryIds = if (isSelected) {
                                                            selectedGlossaryIds - item.id
                                                        } else {
                                                            selectedGlossaryIds + item.id
                                                        }
                                                    } else {
                                                        viewModel.updateGlossaryLastAccessed(item)
                                                        navController.navigate("glossary_detail/${item.id}?source=card")
                                                    }
                                                },
                                                onLongClick = {
                                                    selectedGlossaryIds = if (isSelected) {
                                                        selectedGlossaryIds - item.id
                                                    } else {
                                                        selectedGlossaryIds + item.id
                                                    }
                                                },
                                                isSelected = isSelected,
                                                sharedTransitionScope = sharedTransitionScope,
                                                animatedVisibilityScope = animatedVisibilityScope,
                                                navAnimatedVisibilityScope = navAnimatedVisibilityScope,
                                                sourceKey = "card"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            LazyColumn(
                                state = listState2,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 120.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (allLiteratureItems.isEmpty()) {
                                    item {
                                        EmptyStateMessage("Noch keine Literatur vorhanden.")
                                    }
                                } else {
                                    itemsIndexed(
                                        items = allLiteratureItems,
                                        key = { _, item -> "lit_${item.id}" }
                                    ) { index, item ->
                                        Column {
                                            val isSelected = selectedLiteratureIds.contains(item.id)
                                            LiteratureCard(
                                                item = item,
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        selectedLiteratureIds = if (isSelected) {
                                                            selectedLiteratureIds - item.id
                                                        } else {
                                                            selectedLiteratureIds + item.id
                                                        }
                                                    } else {
                                                        viewModel.updateLiteratureLastAccessed(item)
                                                        navController.navigate("literature_detail/${item.id}?source=card")
                                                    }
                                                },
                                                onLongClick = {
                                                    selectedLiteratureIds = if (isSelected) {
                                                        selectedLiteratureIds - item.id
                                                    } else {
                                                        selectedLiteratureIds + item.id
                                                    }
                                                },
                                                isSelected = isSelected,
                                                sharedTransitionScope = sharedTransitionScope,
                                                animatedVisibilityScope = animatedVisibilityScope,
                                                navAnimatedVisibilityScope = navAnimatedVisibilityScope,
                                                sourceKey = "card"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
