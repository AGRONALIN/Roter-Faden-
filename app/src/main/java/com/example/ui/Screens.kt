package com.example.ui

import androidx.compose.ui.draw.shadow
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.navigation.NavController
import com.example.data.Argument
import com.example.ui.theme.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.animation.ExperimentalAnimationApi

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val recentArguments by viewModel.recentArguments.collectAsState()
    val topRecentArguments = recentArguments.take(3)
    val recentCategories = recentArguments.map { it.category }.filter { it.isNotBlank() }.distinct().take(3)
    val recentGlossaries by viewModel.recentGlossaryItems.collectAsState()
    val topRecentGlossaries = recentGlossaries.take(3)
    
    var randomArgument by remember(recentArguments) { 
        mutableStateOf(if (recentArguments.isNotEmpty()) recentArguments.random() else null)
    }

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
    val headerBgColor by androidx.compose.animation.animateColorAsState(if (isScrolled) Color.White.copy(alpha = 0.35f) else Color.Transparent)
    val headerGlowAlpha by androidx.compose.animation.core.animateFloatAsState(if (isScrolled) 1f else 0f)
    val headerBorderColor by androidx.compose.animation.animateColorAsState(if (isScrolled) ImmersiveBorder.copy(alpha = 0.4f) else ImmersiveBorder.copy(alpha = 0f))

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

    LaunchedEffect(Unit) {
        listState.scrollToItem(0)
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalFadingEdge(topEdge = 20.dp, bottomEdge = 40.dp),
                contentPadding = PaddingValues(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 280.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (topRecentArguments.isEmpty() && topRecentGlossaries.isEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            EmptyStateMessage("Noch keine Inhalte vorhanden.")
                        }
                    }
                } else {
                    if (recentCategories.isNotEmpty()) {
                        item {
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            ) {
                                items(recentCategories) { category ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(percent = 50))
                                            .background(ImmersiveGreen.copy(alpha = 0.15f))
                                            .bounceClick { 
                                                viewModel.updateSearchQuery(category)
                                                navController.navigate("search") 
                                            }
                                            .padding(horizontal = 20.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = category,
                                            color = ImmersiveTextPrimary,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (topRecentArguments.isNotEmpty()) {
                        item {
                            Box(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 8.dp)) {
                                SectionHeader("Argumente")
                            }
                        }
                        itemsIndexed(
                            items = topRecentArguments,
                            key = { _, argument -> argument.id }
                        ) { index, argument ->
                            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                ArgumentCard(
                                    argument = argument,
                                    onClick = {
                                        viewModel.updateArgumentLastAccessed(argument)
                                        navController.navigate("argument_detail/${argument.id}")
                                    },
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }
                        }
                    }

                    if (topRecentGlossaries.isNotEmpty()) {
                        item {
                            Box(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 8.dp)) {
                                SectionHeader("Glossar")
                            }
                        }
                        
                        itemsIndexed(
                            items = topRecentGlossaries,
                            key = { _, item -> "gloss_${item.id}" }
                        ) { index, item ->
                            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
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

            // Floating Header
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, 
                        start = 12.dp,
                        end = 24.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Main Header Pill
                Box(
                    modifier = Modifier
                        .whiteGlowPill(headerBgColor, headerBorderColor, headerGlowAlpha)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Roter Faden",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = ImmersiveGreen
                        )
                    )
                }

                // Subtitle Pill
                Box(
                    modifier = Modifier
                        .whiteGlowPill(headerBgColor, headerBorderColor, headerGlowAlpha)
                        .padding(start = 18.dp, end = 16.dp, top = 6.dp, bottom = 6.dp)
                ) {
                    Text(
                        text = "Setzt eure Herzen in Brannt",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ImmersiveTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 'Zuletzt' Pill
                    Box(
                        modifier = Modifier
                            .whiteGlowPill(headerBgColor, headerBorderColor, headerGlowAlpha)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Zuletzt",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                    
                    if (randomArgument != null) {
                        with(sharedTransitionScope) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(key = "random-${randomArgument!!.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                        clipInOverlayDuringTransition = OverlayClip(androidx.compose.foundation.shape.CircleShape),
                                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                                        boundsTransform = { _, _ -> androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 200f) }
                                    )
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(ImmersiveGreen)
                                    .bounceClick {
                                        viewModel.updateArgumentLastAccessed(randomArgument!!)
                                        navController.navigate("argument_detail/${randomArgument!!.id}?source=random")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "☭",
                                    color = Color.White,
                                    fontSize = 40.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ArgumentCard(
    argument: Argument,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sourceKey: String = "card"
) {
    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "${sourceKey}-${argument.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(12.dp)),
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                    boundsTransform = { _, _ -> androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 200f) }
                )
                .background(if (isSelected) ImmersiveGreen.copy(alpha = 0.2f) else CreamRed, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .bounceClick(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(vertical = 12.dp, horizontal = 12.dp)
        ) {
            Text(
                text = argument.antiMarxistStatement,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary,
                    lineHeight = 28.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = argument.marxistCounterArgument,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ImmersiveTextSecondary,
                    lineHeight = 20.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GlossaryCard(
    item: com.example.data.GlossaryItem,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sourceKey: String = "card"
) {
    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "gloss_${sourceKey}-${item.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(12.dp)),
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                    boundsTransform = { _, _ -> androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 200f) }
                )
                .background(ImmersivePillBg, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .bounceClick(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 12.dp)
        ) {
            Text(
                text = item.term,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary,
                    lineHeight = 28.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.definition,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ImmersiveTextSecondary,
                    lineHeight = 20.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SearchScreen(
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchGlossary by viewModel.searchGlossaryResults.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
    val headerBgColor by androidx.compose.animation.animateColorAsState(if (isScrolled) Color.White.copy(alpha = 0.35f) else Color.Transparent)
    val headerGlowAlpha by androidx.compose.animation.core.animateFloatAsState(if (isScrolled) 1f else 0f)
    val headerBorderColor by androidx.compose.animation.animateColorAsState(if (isScrolled) ImmersiveBorder.copy(alpha = 0.4f) else ImmersiveBorder.copy(alpha = 0f))

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

    LaunchedEffect(Unit) {
        viewModel.focusSearchEvent.collect {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalFadingEdge(topEdge = 20.dp, bottomEdge = 40.dp)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 90.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Argumente oder Glossar durchsuchen...") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = ImmersiveSurface,
                            unfocusedContainerColor = ImmersiveSurface
                        )
                    )
                }

                if (searchQuery.isNotBlank()) {
                    if (searchResults.isNotEmpty()) {
                        item { SectionHeader("Argumente") }
                        itemsIndexed(
                                items = searchResults,
                                key = { _, argument -> "arg_${argument.id}" }
                            ) { index, argument ->
                                Column {
                                    ArgumentCard(
                                        argument = argument,
                                        onClick = { navController.navigate("argument_detail/${argument.id}") },
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedVisibilityScope = animatedVisibilityScope
                                    )
                                }
                            }
                        }

                        if (searchGlossary.isNotEmpty()) {
                            item { SectionHeader("Glossar") }
                            itemsIndexed(
                                items = searchGlossary,
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

                        if (searchResults.isEmpty() && searchGlossary.isEmpty()) {
                            item { EmptyStateMessage("Keine Ergebnisse gefunden.") }
                        }
                    } else {
                        item { EmptyStateMessage("Tippe, um zu suchen.") }
                    }
                }

            // Floating Header Pill
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, start = 12.dp)
                    .whiteGlowPill(headerBgColor, headerBorderColor, headerGlowAlpha)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Suche",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = ImmersiveTextPrimary,
                        letterSpacing = (-1).sp
                    )
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = ImmersiveTextSecondary,
            letterSpacing = 1.5.sp
        ),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = ImmersiveTextSecondary,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun Modifier.whiteGlowPill(headerBgColor: Color, headerBorderColor: Color, headerGlowAlpha: Float): Modifier = this
    .shadow(
        elevation = (24 * headerGlowAlpha).dp,
        shape = RoundedCornerShape(percent = 50),
        spotColor = Color.White.copy(alpha = 0.6f),
        ambientColor = Color.White.copy(alpha = 0.6f)
    )
    .clip(RoundedCornerShape(percent = 50))
    .background(
        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
            colors = listOf(
                headerBgColor.copy(alpha = headerBgColor.alpha * 0.05f),
                headerBgColor.copy(alpha = (headerBgColor.alpha * 1.5f).coerceAtMost(1f)),
                headerBgColor.copy(alpha = headerBgColor.alpha * 0.05f)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
            colors = listOf(
                headerBorderColor.copy(alpha = headerBorderColor.alpha * 0.05f),
                headerBorderColor,
                headerBorderColor.copy(alpha = headerBorderColor.alpha * 0.05f)
            )
        ),
        shape = RoundedCornerShape(percent = 50)
    )
