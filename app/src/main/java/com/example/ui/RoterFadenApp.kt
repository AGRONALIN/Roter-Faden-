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
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
        val isDarkTheme by viewModel.isDarkTheme.collectAsState()

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
            val startDest = remember { if (viewModel.showOnboarding.value) "onboarding" else "main" }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startDest,
                    enterTransition = { fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)) },
                    exitTransition = { fadeOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)) },
                    popEnterTransition = { fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)) },
                    popExitTransition = { fadeOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)) }
                ) {
                    composable("onboarding") {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onFinish = {
                                viewModel.completeOnboarding()
                                navController.navigate("main") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("main") {
                        val mainScope = this@composable

                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    slideInHorizontally(animationSpec = spring(stiffness = 200f, dampingRatio = 0.85f)) { width -> width }.togetherWith(
                                        slideOutHorizontally(animationSpec = spring(stiffness = 200f, dampingRatio = 0.85f)) { width -> -width }
                                    ).using(SizeTransform(clip = false))
                                } else {
                                    slideInHorizontally(animationSpec = spring(stiffness = 200f, dampingRatio = 0.85f)) { width -> -width }.togetherWith(
                                        slideOutHorizontally(animationSpec = spring(stiffness = 200f, dampingRatio = 0.85f)) { width -> width }
                                    ).using(SizeTransform(clip = false))
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    var dragAccumulator = 0f
                                    detectHorizontalDragGestures(
                                        onDragStart = { dragAccumulator = 0f },
                                        onDragEnd = {
                                            if (dragAccumulator > 120f && currentTab > 0) {
                                                currentTab -= 1
                                            } else if (dragAccumulator < -120f && currentTab < 2) {
                                                currentTab += 1
                                            }
                                        },
                                        onDragCancel = {},
                                        onHorizontalDrag = { change, dragAmount ->
                                            change.consume()
                                            dragAccumulator += dragAmount
                                        }
                                    )
                                },
                            label = "tab_transition"
                        ) { page ->
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
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    
    val targetNavBarColor = if (isDarkTheme) Color(0xFF3C1F1B) else Color(255, 195, 185)
    val navBarColor by animateColorAsState(targetNavBarColor, animationSpec = tween(400), label = "navBarColor")
    val navBarBorderColor = ImmersiveTextSecondary.copy(alpha = 0.35f)
    
    val density = LocalDensity.current
    var navBarLeftDp by remember { mutableStateOf(24.dp) }
    
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
                    .onGloballyPositioned { coordinates ->
                        val positionInRoot = coordinates.positionInRoot()
                        val leftInDp = with(density) { positionInRoot.x.toDp() }
                        if (leftInDp > 0.dp) {
                            navBarLeftDp = leftInDp
                        }
                    }
                    .clip(CircleShape)
                    .background(navBarColor) // Frosted Cream Rot
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
        
            val animatedPaddingEnd by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (expanded) 24.dp else navBarLeftDp,
                label = "fabPaddingEnd"
            )

            // Expandable FAB on the right
            AnimatedVisibility(
                visible = currentTab != 2,
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                enter = androidx.compose.animation.slideInHorizontally(initialOffsetX = { -150 }, animationSpec = spring(stiffness = 100f, dampingRatio = 0.8f)) + fadeIn(animationSpec = tween(600)),
                exit = androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -150 }, animationSpec = spring(stiffness = 100f, dampingRatio = 0.8f)) + fadeOut(animationSpec = tween(600))
            ) {
                val fabVisibilityScope = this
                var showItem1 by remember { mutableStateOf(expanded) }
                var showItem2 by remember { mutableStateOf(expanded) }
                var showItem3 by remember { mutableStateOf(expanded) }
                var containerActivelyVisible by remember { mutableStateOf(expanded) }

                LaunchedEffect(expanded) {
                    if (expanded) {
                        containerActivelyVisible = true
                        showItem3 = true
                        kotlinx.coroutines.delay(100)
                        showItem2 = true
                        kotlinx.coroutines.delay(100)
                        showItem1 = true
                    } else {
                        showItem1 = false
                        kotlinx.coroutines.delay(80)
                        showItem2 = false
                        kotlinx.coroutines.delay(80)
                        showItem3 = false
                        kotlinx.coroutines.delay(250)
                        containerActivelyVisible = false
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (containerActivelyVisible || expanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(bottom = 8.dp, end = 24.dp)
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showItem1,
                                enter = fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing)) + 
                                        scaleIn(initialScale = 0.8f, animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                                        slideInVertically(initialOffsetY = { 30 }, animationSpec = tween(250, easing = FastOutSlowInEasing)),
                                exit = fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) + 
                                       scaleOut(targetScale = 0.8f, animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                       slideOutVertically(targetOffsetY = { 15 }, animationSpec = tween(200, easing = FastOutSlowInEasing))
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
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(navBarColor)
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
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = showItem2,
                                enter = fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing)) + 
                                        scaleIn(initialScale = 0.8f, animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                                        slideInVertically(initialOffsetY = { 30 }, animationSpec = tween(250, easing = FastOutSlowInEasing)),
                                exit = fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) + 
                                       scaleOut(targetScale = 0.8f, animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                       slideOutVertically(targetOffsetY = { 15 }, animationSpec = tween(200, easing = FastOutSlowInEasing))
                            ) {
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
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(navBarColor)
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
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = showItem3,
                                enter = fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing)) + 
                                        scaleIn(initialScale = 0.8f, animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                                        slideInVertically(initialOffsetY = { 30 }, animationSpec = tween(250, easing = FastOutSlowInEasing)),
                                exit = fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) + 
                                       scaleOut(targetScale = 0.8f, animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                       slideOutVertically(targetOffsetY = { 15 }, animationSpec = tween(200, easing = FastOutSlowInEasing))
                            ) {
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
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(navBarColor)
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
                    }

                    Box(modifier = Modifier.padding(end = animatedPaddingEnd).size(64.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !expanded,
                            enter = scaleIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f)) + fadeIn(),
                            exit = scaleOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f)) + fadeOut()
                        ) {
                            val innerVisibilityScope = this
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(navBarColor)
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
