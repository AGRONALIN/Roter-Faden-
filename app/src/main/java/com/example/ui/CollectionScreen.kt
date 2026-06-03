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
import com.example.data.staticLiteratures
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.zIndex

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CollectionScreen(
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Argumente", "Glossar", "Literatur")

    val allArguments by viewModel.recentArguments.collectAsState()
    val allGlossaryItems by viewModel.glossaryItems.collectAsState()

    var selectedArgumentIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedArgumentIds.isNotEmpty()

    var showDeleteDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = isSelectionMode) {
        selectedArgumentIds = emptySet()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.setBottomBarVisible(true)
        }
    }

    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousScrollOffset = listState.firstVisibleItemScrollOffset
        androidx.compose.runtime.snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
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

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val jsonStr = viewModel.exportArguments(selectedArgumentIds)
                context.contentResolver.openOutputStream(it)?.use { out ->
                    out.write(jsonStr.toByteArray())
                }
                selectedArgumentIds = emptySet()
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Wirklich löschen?") },
            text = { Text("Möchtest du diese ${selectedArgumentIds.size} Einträge endgültig löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteArgumentsById(selectedArgumentIds)
                    selectedArgumentIds = emptySet()
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
                with(sharedTransitionScope) {
                    Column(
                        modifier = Modifier
                            .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 10f)
                            .zIndex(10f)
                            .background(ImmersiveBackground)
                    ) {
                        Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp))
                        
                        // Top header - Title and settings icon
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
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left action button (Shuffle style used for Export)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(ImmersiveGreen)
                                        .bounceClick {
                                            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                            val fileName = "debattenbank_export_${dateFormat.format(Date())}.json"
                                            exportLauncher.launch(fileName)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Upload, contentDescription = "Export", modifier = Modifier.size(20.dp), tint = ImmersiveOnGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Export", fontWeight = FontWeight.Bold, color = ImmersiveOnGreen)
                                    }
                                }
                                
                                // Right icons
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
                                            .bounceClick { selectedArgumentIds = emptySet() }
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Cancel", modifier = Modifier.size(24.dp), tint = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                AnimatedContent(
                    modifier = Modifier.fillMaxSize().weight(1f).padding(horizontal = 24.dp).clipToBounds().verticalFadingEdge(topEdge = 20.dp, bottomEdge = 40.dp),
                    targetState = selectedTabIndex,
                    transitionSpec = {
                        val springSpec = androidx.compose.animation.core.spring<androidx.compose.ui.unit.IntOffset>(
                            dampingRatio = 0.8f,
                            stiffness = 300f
                        )
                        val fadeSpec = tween<Float>(300)

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
                                state = listState,
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
                                                        navController.navigate("argument_detail/${argument.id}")
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
                                                animatedVisibilityScope = animatedVisibilityScope
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            LazyColumn(
                                state = listState,
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
                                            GlossaryCard(
                                                item = item,
                                                onClick = {
                                                    viewModel.updateGlossaryLastAccessed(item)
                                                    navController.navigate("glossary_detail/${item.id}")
                                                },
                                                sharedTransitionScope = sharedTransitionScope,
                                                animatedVisibilityScope = animatedVisibilityScope
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 120.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (staticLiteratures.isEmpty()) {
                                    item {
                                        EmptyStateMessage("Noch keine Literatur vorhanden.")
                                    }
                                } else {
                                    itemsIndexed(
                                        items = staticLiteratures,
                                        key = { _, item -> "lit_${item.id}" }
                                    ) { index, item ->
                                        Column {
                                            LiteratureCard(
                                                item = item,
                                                onClick = {
                                                    navController.navigate("literature_detail/${item.id}")
                                                },
                                                sharedTransitionScope = sharedTransitionScope,
                                                animatedVisibilityScope = animatedVisibilityScope
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
