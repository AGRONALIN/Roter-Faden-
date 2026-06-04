package com.example.ui

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import com.example.data.Argument
import com.example.data.GlossaryItem
import com.example.ui.theme.*
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay

fun getTabIndex(route: String): Int {
    return when {
        route == "home" -> 0
        route == "collection" -> 1
        route == "search" -> 2
        else -> -1
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RoterFadenApp(viewModel: AppViewModel) {
    val navController = rememberNavController()
    var currentTab by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    SharedTransitionLayout {
        val immersiveBg = ImmersiveBackground
        val immersiveGreenColor = ImmersiveGreen
        val immersiveTextSec = ImmersiveTextSecondary
        
        val slideUpEnter: (AnimatedContentTransitionScope<*>.() -> EnterTransition) = {
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
            ) + fadeIn(animationSpec = tween(300))
        }
        val slideDownExit: (AnimatedContentTransitionScope<*>.() -> ExitTransition) = {
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
            ) + fadeOut(animationSpec = tween(250))
        }

        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
                .background(immersiveBg)
        ) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "main",
                    enterTransition = { fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)) },
                    exitTransition = { fadeOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)) },
                    popEnterTransition = { fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)) },
                    popExitTransition = { fadeOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)) }
                ) {
                    composable("main") {
                        val transitionState = remember { SeekableTransitionState(currentTab) }
                        val transition = rememberTransition(transitionState, label = "tab_transition")
                        val screenWidth = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

                        LaunchedEffect(currentTab) {
                            if (transitionState.currentState != currentTab && transitionState.targetState != currentTab) {
                                transitionState.animateTo(currentTab, animationSpec = spring(stiffness = 40f, dampingRatio = 0.9f))
                            }
                        }

                        transition.AnimatedContent(
                            transitionSpec = {
                                if (targetState > initialState) {
                                    slideInHorizontally(animationSpec = spring(stiffness = 40f, dampingRatio = 0.9f)) { width -> width }.togetherWith(
                                        slideOutHorizontally(animationSpec = spring(stiffness = 40f, dampingRatio = 0.9f)) { width -> -width }
                                    ).using(SizeTransform(clip = false))
                                } else {
                                    slideInHorizontally(animationSpec = spring(stiffness = 40f, dampingRatio = 0.9f)) { width -> -width }.togetherWith(
                                        slideOutHorizontally(animationSpec = spring(stiffness = 40f, dampingRatio = 0.9f)) { width -> width }
                                    ).using(SizeTransform(clip = false))
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    var dragOffset = 0f
                                    var target = currentTab
                                    detectHorizontalDragGestures(
                                        onDragStart = { dragOffset = 0f },
                                        onDragEnd = {
                                            val fraction = (Math.abs(dragOffset) / screenWidth).coerceIn(0f, 1f)
                                            coroutineScope.launch {
                                                if (fraction > 0.10f && target != currentTab) {
                                                    transitionState.animateTo(target, spring(stiffness = 40f, dampingRatio = 0.9f))
                                                    currentTab = target
                                                } else {
                                                    transitionState.animateTo(currentTab, spring(stiffness = 40f, dampingRatio = 0.9f))
                                                }
                                            }
                                        },
                                        onDragCancel = { 
                                            coroutineScope.launch {
                                                transitionState.animateTo(currentTab, spring(stiffness = 40f, dampingRatio = 0.9f))
                                            }
                                        },
                                        onHorizontalDrag = { _, dragAmount ->
                                            dragOffset += dragAmount * 2.2f // Much lower resistance / more sensitive
                                            if (dragOffset < 0 && currentTab < 2) {
                                                target = currentTab + 1
                                            } else if (dragOffset > 0 && currentTab > 0) {
                                                target = currentTab - 1
                                            } else {
                                                target = currentTab
                                            }
                                            
                                            if (target != currentTab) {
                                                val fraction = (Math.abs(dragOffset) / screenWidth).coerceIn(0f, 1f)
                                                coroutineScope.launch {
                                                    transitionState.seekTo(fraction = fraction, targetState = target)
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    transitionState.seekTo(0f, currentTab)
                                                }
                                            }
                                        }
                                    )
                                },
                            contentKey = { it }
                        ) { page ->
                            val mainScope = this@composable
                            when (page) {
                                0 -> HomeScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@AnimatedContent,
                                    navAnimatedVisibilityScope = mainScope,
                                    onNavigateToSearch = { currentTab = 2 }
                                )
                                1 -> CollectionScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@AnimatedContent,
                                    navAnimatedVisibilityScope = mainScope
                                )
                                2 -> SearchScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@AnimatedContent,
                                    navAnimatedVisibilityScope = mainScope
                                )
                            }
                        }
                    }
                    composable(
                        "edit_argument",
                        enterTransition = slideUpEnter,
                        exitTransition = slideDownExit,
                        popEnterTransition = slideUpEnter,
                        popExitTransition = slideDownExit
                    ) {
                        EditArgumentScreen(viewModel, navController, this@SharedTransitionLayout, this@composable, -1)
                    }
                    composable(
                        route = "edit_argument/{argId}",
                        arguments = listOf(navArgument("argId") { type = NavType.IntType }),
                        enterTransition = slideUpEnter,
                        exitTransition = slideDownExit,
                        popEnterTransition = slideUpEnter,
                        popExitTransition = slideDownExit
                    ) { backStackEntry ->
                        val argId = backStackEntry.arguments?.getInt("argId") ?: -1
                        EditArgumentScreen(viewModel, navController, this@SharedTransitionLayout, this@composable, argId)
                    }
                    composable(
                        "edit_glossary",
                        enterTransition = slideUpEnter,
                        exitTransition = slideDownExit,
                        popEnterTransition = slideUpEnter,
                        popExitTransition = slideDownExit
                    ) {
                        EditGlossaryScreen(viewModel, navController, this@SharedTransitionLayout, this@composable)
                    }
                    composable(
                        "edit_literature",
                        enterTransition = slideUpEnter,
                        exitTransition = slideDownExit,
                        popEnterTransition = slideUpEnter,
                        popExitTransition = slideDownExit
                    ) {
                        EditLiteratureScreen(viewModel, navController, this@SharedTransitionLayout, this@composable, -1)
                    }
                    composable(
                        route = "edit_literature/{litId}",
                        arguments = listOf(navArgument("litId") { type = NavType.IntType }),
                        enterTransition = slideUpEnter,
                        exitTransition = slideDownExit,
                        popEnterTransition = slideUpEnter,
                        popExitTransition = slideDownExit
                    ) { backStackEntry ->
                        val litId = backStackEntry.arguments?.getInt("litId") ?: -1
                        EditLiteratureScreen(viewModel, navController, this@SharedTransitionLayout, this@composable, litId)
                    }
                    composable(
                        route = "glossary_detail/{termId}?source={source}",
                        arguments = listOf(
                            navArgument("termId") { type = NavType.IntType },
                            navArgument("source") { 
                                type = NavType.StringType
                                defaultValue = "card"
                            }
                        ),
                        enterTransition = { fadeIn(animationSpec = tween(150)) },
                        exitTransition = { fadeOut(animationSpec = tween(150)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                        popExitTransition = { fadeOut(animationSpec = tween(150)) }
                    ) { backStackEntry ->
                        val termId = backStackEntry.arguments?.getInt("termId") ?: 0
                        val sourceKey = backStackEntry.arguments?.getString("source") ?: "card"
                        val glossary by viewModel.glossaryItems.collectAsState()
                        
                        var lastValidItem by remember { mutableStateOf<GlossaryItem?>(null) }
                        val currentItem = glossary.find { it.id == termId }
                        if (currentItem != null) {
                            lastValidItem = currentItem
                        }

                        val itemToDisplay = lastValidItem

                        if (itemToDisplay != null) {
                            GlossaryDetailScreen(
                                glossaryItem = itemToDisplay,
                                viewModel = viewModel,
                                navController = navController,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@composable,
                                sourceKey = sourceKey
                            )
                        } else {
                            Text("Begriff nicht gefunden", modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
                        }
                    }
                    composable(
                        "songs",
                        enterTransition = slideUpEnter,
                        exitTransition = slideDownExit,
                        popEnterTransition = slideUpEnter,
                        popExitTransition = slideDownExit
                    ) {
                        SongsScreen(viewModel, navController, this@SharedTransitionLayout, this@composable)
                    }
                    composable(
                        route = "literature_detail/{litId}?source={source}",
                        arguments = listOf(
                            navArgument("litId") { type = NavType.IntType },
                            navArgument("source") { 
                                type = NavType.StringType
                                defaultValue = "card"
                            }
                        ),
                        enterTransition = { fadeIn(animationSpec = tween(150)) },
                        exitTransition = { fadeOut(animationSpec = tween(150)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                        popExitTransition = { fadeOut(animationSpec = tween(150)) }
                    ) { backStackEntry ->
                        val litId = backStackEntry.arguments?.getInt("litId") ?: 0
                        val sourceKey = backStackEntry.arguments?.getString("source") ?: "card"
                        
                        val literatureList by viewModel.literatureList.collectAsState()
                        var lastValidItem by remember { mutableStateOf<com.example.data.LiteratureItem?>(null) }
                        val currentItem = literatureList.find { it.id == litId }
                        if (currentItem != null) {
                            lastValidItem = currentItem
                        }

                        val itemToDisplay = lastValidItem

                        if (itemToDisplay != null) {
                            LiteratureDetailScreen(
                                literatureItem = itemToDisplay,
                                viewModel = viewModel,
                                navController = navController,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@composable,
                                sourceKey = sourceKey
                            )
                        } else {
                            Text("Literatur nicht gefunden", modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
                        }
                    }
                    composable(
                        route = "argument_detail/{argId}?source={source}",
                        arguments = listOf(
                            navArgument("argId") { type = NavType.IntType },
                            navArgument("source") { 
                                type = NavType.StringType
                                defaultValue = "card"
                            }
                        ),
                        enterTransition = { fadeIn(animationSpec = tween(150)) },
                        exitTransition = { fadeOut(animationSpec = tween(150)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                        popExitTransition = { fadeOut(animationSpec = tween(150)) }
                    ) { backStackEntry ->
                        val argId = backStackEntry.arguments?.getInt("argId") ?: 0
                        val sourceKey = backStackEntry.arguments?.getString("source") ?: "card"
                        val recentArgs by viewModel.recentArguments.collectAsState()
                        
                        var lastValidArg by remember { mutableStateOf<Argument?>(null) }
                        val currentArg = recentArgs.find { it.id == argId }
                        if (currentArg != null) {
                            lastValidArg = currentArg
                        }

                        val argumentToDisplay = lastValidArg

                        if (argumentToDisplay != null) {
                            ArgumentDetailScreen(
                                argument = argumentToDisplay,
                                viewModel = viewModel,
                                navController = navController,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@composable,
                                sourceKey = sourceKey
                            )
                        } else {
                            Text("Argument nicht gefunden", modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
                        }
                    }
                }
                
                // Top Blur Effect (Edge-to-Edge illusion)
                var fabExpanded by remember { mutableStateOf(false) }

                if (fabExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(10f)
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { fabExpanded = false })
                            }
                    )
                }

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .zIndex(90f)
                    .background(Brush.verticalGradient(
                        colors = listOf(ImmersiveBackground, Color.Transparent)
                    ))
                    .align(Alignment.TopCenter)
                )

                Box(modifier = Modifier.align(Alignment.BottomCenter).renderInSharedTransitionScopeOverlay(zIndexInOverlay = 100f).zIndex(100f)) {
                    this@SharedTransitionLayout.RoterFadenBottomNav(
                        navController = navController, 
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        coroutineScope = coroutineScope,
                        viewModel = viewModel,
                        expanded = fabExpanded,
                        onExpandedChange = { fabExpanded = it }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.RoterFadenBottomNav(
    navController: NavController, 
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    viewModel: AppViewModel,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isBottomBarVisible by viewModel.isBottomBarVisible.collectAsState()
    val isVisible = currentRoute == "main" && isBottomBarVisible
    
    val navBarColor = CreamRed
    val navBarBorderColor = ImmersiveBorder.copy(alpha = 0.4f)
    
    AnimatedVisibility(
        visible = isVisible,
        modifier = Modifier.zIndex(100f),
        enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }, animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f)) + fadeIn(animationSpec = tween(600)),
        exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }, animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f)) + fadeOut(animationSpec = tween(600))
    ) {
        val animatedVisibilityScope = this
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
                BackHandler(enabled = expanded) {
                    onExpandedChange(false)
                }

                val isFabVisible = currentTab != 2
                val navBarOffset by androidx.compose.animation.core.animateDpAsState(
                    targetValue = if (isFabVisible && !expanded) (-44).dp else 0.dp,
                    label = "navBarOffset",
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f)
                )

            // Nav Bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = navBarOffset)
                    .zIndex(100f)
                    .shadow(24.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.15f), spotColor = Color.Black.copy(alpha = 0.3f))
                    .clip(CircleShape)
                    .background(navBarColor) // Frosted Cream Rot
                    .border(1.dp, navBarBorderColor, CircleShape)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isHome = currentTab == 0
                val isSearch = currentTab == 2
                val isCollection = currentTab == 1

                NavItem(
                    icon = Icons.Filled.Home,
                    label = "Home",
                    isSelected = isHome,
                    onClick = {
                        if (!isHome) {
                            onTabSelected(0)
                        }
                    }
                )

                NavItem(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Alle",
                    isSelected = isCollection,
                    onClick = {
                        if (!isCollection) {
                            onTabSelected(1)
                        }
                    }
                )
                
                NavItem(
                    icon = Icons.Filled.Search,
                    label = "Suche",
                    isSelected = isSearch,
                    onClick = {
                        if (!isSearch) {
                            onTabSelected(2)
                        } else {
                            viewModel.triggerSearchFocus()
                        }
                    }
                )
            }
        
            // Expandable FAB on the right
            AnimatedVisibility(
                visible = currentTab != 2,
                modifier = Modifier.align(Alignment.BottomEnd),
                enter = androidx.compose.animation.slideInHorizontally(initialOffsetX = { -150 }, animationSpec = spring(stiffness = 100f, dampingRatio = 0.8f)) + fadeIn(animationSpec = tween(600)),
                exit = androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -150 }, animationSpec = spring(stiffness = 100f, dampingRatio = 0.8f)) + fadeOut(animationSpec = tween(600))
            ) {
                val fabVisibilityScope = this
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedVisibility(
                        visible = expanded,
                        enter = scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 150f)) + fadeIn(animationSpec = tween(600)),
                        exit = scaleOut(targetScale = 0.8f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 150f)) + fadeOut(animationSpec = tween(600))
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(key = "fab_neuer_begriff"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(contentScale = androidx.compose.ui.layout.ContentScale.Crop),
                                        boundsTransform = { _, _ -> androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 100f) }
                                    )
                                    .shadow(16.dp, RoundedCornerShape(percent = 50), ambientColor = Color.Black.copy(alpha = 0.15f), spotColor = Color.Black.copy(alpha = 0.3f))
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(navBarColor)
                                    .border(1.dp, navBarBorderColor, RoundedCornerShape(percent = 50))
                                    .bounceClick {
                                        onExpandedChange(false)
                                        navController.navigate("edit_glossary")
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Neuer Begriff",
                                    color = ImmersiveTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(ImmersiveGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.AutoStories, contentDescription = "Begriff", modifier = Modifier.size(20.dp), tint = ImmersiveOnGreen)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(key = "fab_neue_literatur"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(contentScale = androidx.compose.ui.layout.ContentScale.Crop),
                                        boundsTransform = { _, _ -> androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 100f) }
                                    )
                                    .shadow(16.dp, RoundedCornerShape(percent = 50), ambientColor = Color.Black.copy(alpha = 0.15f), spotColor = Color.Black.copy(alpha = 0.3f))
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(navBarColor)
                                    .border(1.dp, navBarBorderColor, RoundedCornerShape(percent = 50))
                                    .bounceClick {
                                        onExpandedChange(false)
                                        navController.navigate("edit_literature")
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Neue Literatur",
                                    color = ImmersiveTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(ImmersiveGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.AutoStories, contentDescription = "Literatur", modifier = Modifier.size(20.dp), tint = ImmersiveOnGreen)
                                }
                            }
                            
                            Row(
                                modifier = Modifier
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(key = "fab_neues_argument"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(contentScale = androidx.compose.ui.layout.ContentScale.Crop),
                                        boundsTransform = { _, _ -> androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 100f) }
                                    )
                                    .shadow(16.dp, RoundedCornerShape(percent = 50), ambientColor = Color.Black.copy(alpha = 0.15f), spotColor = Color.Black.copy(alpha = 0.3f))
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(navBarColor)
                                    .border(1.dp, navBarBorderColor, RoundedCornerShape(percent = 50))
                                    .bounceClick {
                                        onExpandedChange(false)
                                        navController.navigate("edit_argument")
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Neues Argument",
                                    color = ImmersiveTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(ImmersiveGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Argument", modifier = Modifier.size(20.dp), tint = ImmersiveOnGreen)
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !expanded,
                            enter = scaleIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f)) + fadeIn(),
                            exit = scaleOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f)) + fadeOut()
                        ) {
                            val innerVisibilityScope = this
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .shadow(24.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.15f), spotColor = Color.Black.copy(alpha = 0.3f))
                                    .clip(CircleShape)
                                    .background(navBarColor)
                                    .border(1.dp, navBarBorderColor, CircleShape)
                                    .bounceClick { onExpandedChange(true) },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(CircleShape).background(ImmersiveGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Hinzufügen",
                                        tint = ImmersiveOnGreen,
                                        modifier = Modifier.size(24.dp)
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

@Composable
fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) ImmersiveGreen else Color.Transparent,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f)
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) ImmersiveOnGreen else ImmersiveTextSecondary,
        animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
    )

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = contentColor)
        AnimatedVisibility(
            visible = isSelected,
            enter = expandHorizontally(expandFrom = Alignment.Start, animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f)) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start, animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f)) + fadeOut()
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(start = 8.dp),
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
