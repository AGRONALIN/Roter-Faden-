package com.example.ui

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
import com.example.data.Argument
import com.example.data.GlossaryItem
import com.example.ui.theme.*
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

    SharedTransitionLayout {
        val immersiveBg = ImmersiveBackground
        val immersiveGreenColor = ImmersiveGreen
        val immersiveTextSec = ImmersiveTextSecondary
        
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
                .background(immersiveBg)
                .drawBehind {
                    val path1 = androidx.compose.ui.graphics.Path()
                    path1.moveTo(size.width, 0f)
                    path1.lineTo(size.width, size.height * 0.4f)
                    path1.lineTo(size.width * 0.3f, 0f)
                    path1.close()
                    drawPath(path1, color = immersiveGreenColor.copy(alpha = 0.04f))

                    val path3 = androidx.compose.ui.graphics.Path()
                    path3.moveTo(0f, size.height * 0.2f)
                    path3.lineTo(size.width * 0.5f, size.height * 0.45f)
                    path3.lineTo(0f, size.height * 0.3f)
                    path3.close()
                    drawPath(path3, color = immersiveTextSec.copy(alpha = 0.02f))
                }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    enterTransition = {
                        val initial = initialState.destination.route ?: ""
                        val target = targetState.destination.route ?: ""
                        val initialTab = getTabIndex(initial)
                        val targetTab = getTabIndex(target)

                        if (initialTab != -1 && targetTab != -1 && initialTab != targetTab) {
                            if (targetTab > initialTab) {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                            } else {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                            }
                        } else {
                            fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.9f, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                        }
                    },
                    exitTransition = {
                        val initial = initialState.destination.route ?: ""
                        val target = targetState.destination.route ?: ""
                        val initialTab = getTabIndex(initial)
                        val targetTab = getTabIndex(target)

                        if (initialTab != -1 && targetTab != -1 && initialTab != targetTab) {
                            if (targetTab > initialTab) {
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                            } else {
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                            }
                        } else {
                            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 1.1f, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                        }
                    },
                    popEnterTransition = {
                        val initial = initialState.destination.route ?: ""
                        val target = targetState.destination.route ?: ""
                        val initialTab = getTabIndex(initial)
                        val targetTab = getTabIndex(target)

                        if (initialTab != -1 && targetTab != -1 && initialTab != targetTab) {
                            if (targetTab > initialTab) {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                            } else {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                            }
                        } else {
                            fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 1.1f, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                        }
                    },
                    popExitTransition = {
                        val initial = initialState.destination.route ?: ""
                        val target = targetState.destination.route ?: ""
                        val initialTab = getTabIndex(initial)
                        val targetTab = getTabIndex(target)

                        if (initialTab != -1 && targetTab != -1 && initialTab != targetTab) {
                            if (targetTab > initialTab) {
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                            } else {
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                            }
                        } else {
                            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.9f, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                        }
                    }
                ) {
                    composable("home") {
                        HomeScreen(viewModel, navController, this@SharedTransitionLayout, this@composable)
                    }
                    composable("search") {
                        SearchScreen(viewModel, navController, this@SharedTransitionLayout, this@composable)
                    }
                    composable("collection") {
                        CollectionScreen(viewModel, navController, this@SharedTransitionLayout, this@composable)
                    }
                    composable("edit_argument") {
                        EditArgumentScreen(viewModel, navController, this@SharedTransitionLayout, this@composable, -1)
                    }
                    composable(
                        route = "edit_argument/{argId}",
                        arguments = listOf(navArgument("argId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val argId = backStackEntry.arguments?.getInt("argId") ?: -1
                        EditArgumentScreen(viewModel, navController, this@SharedTransitionLayout, this@composable, argId)
                    }
                    composable("edit_glossary") {
                        EditGlossaryScreen(viewModel, navController, this@SharedTransitionLayout, this@composable)
                    }
                    composable(
                        route = "glossary_detail/{termId}?source={source}",
                        arguments = listOf(
                            navArgument("termId") { type = NavType.IntType },
                            navArgument("source") { 
                                type = NavType.StringType
                                defaultValue = "card"
                            }
                        )
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
                        route = "argument_detail/{argId}?source={source}",
                        arguments = listOf(
                            navArgument("argId") { type = NavType.IntType },
                            navArgument("source") { 
                                type = NavType.StringType
                                defaultValue = "card"
                            }
                        )
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
                    .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
                    .zIndex(90f)
                    .background(Brush.verticalGradient(
                        colors = listOf(ImmersiveBackground, Color.Transparent)
                    ))
                    .align(Alignment.TopCenter)
                )

                Box(modifier = Modifier.align(Alignment.BottomCenter).renderInSharedTransitionScopeOverlay(zIndexInOverlay = 100f).zIndex(100f)) {
                    this@SharedTransitionLayout.RoterFadenBottomNav(
                        navController = navController, 
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
    viewModel: AppViewModel,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isBottomBarVisible by viewModel.isBottomBarVisible.collectAsState()
    val isVisible = (currentRoute == "home" || currentRoute == "search" || currentRoute == "collection") && isBottomBarVisible
    
    val navBarColor = CreamRed
    val navBarBorderColor = ImmersiveBorder.copy(alpha = 0.4f)
    
    AnimatedVisibility(
        visible = isVisible,
        modifier = Modifier.zIndex(100f),
        enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(animationSpec = tween(300)),
        exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeOut(animationSpec = tween(300))
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

                val isFabVisible = currentRoute != "search"
                val navBarOffset by androidx.compose.animation.core.animateDpAsState(
                    targetValue = if (isFabVisible && !expanded) (-44).dp else 0.dp,
                    label = "navBarOffset",
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                )

            // Nav Bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = navBarOffset)
                    .zIndex(100f)
                    .shadow(8.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.1f))
                    .clip(CircleShape)
                    .background(navBarColor) // Frosted Cream Rot
                    .border(1.dp, navBarBorderColor, CircleShape)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isHome = currentRoute == "home"
                val isSearch = currentRoute == "search"
                val isCollection = currentRoute == "collection"

                NavItem(
                    icon = Icons.Filled.Home,
                    label = "Home",
                    isSelected = isHome,
                    onClick = {
                        if (!isHome) {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                )

                NavItem(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Alle",
                    isSelected = isCollection,
                    onClick = {
                        if (!isCollection) {
                            navController.navigate("collection") {
                                popUpTo("home")
                            }
                        }
                    }
                )
                
                NavItem(
                    icon = Icons.Filled.Search,
                    label = "Suche",
                    isSelected = isSearch,
                    onClick = {
                        if (!isSearch) {
                            navController.navigate("search")
                        } else {
                            viewModel.triggerSearchFocus()
                        }
                    }
                )
            }
        
            // Expandable FAB on the right
            AnimatedVisibility(
                visible = currentRoute != "search",
                modifier = Modifier.align(Alignment.BottomEnd),
                enter = androidx.compose.animation.slideInHorizontally(initialOffsetX = { -50 }, animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)) + fadeIn(animationSpec = tween(300)),
                exit = androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -50 }, animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)) + fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedVisibility(
                        visible = expanded,
                        enter = scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f)) + fadeIn(animationSpec = tween(200)),
                        exit = scaleOut(targetScale = 0.8f, animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
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
                                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                                        boundsTransform = { _, _ -> androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 200f) }
                                    )
                                    .shadow(6.dp, RoundedCornerShape(percent = 50), ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.1f))
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
                                        sharedContentState = rememberSharedContentState(key = "fab_neues_argument"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                                        boundsTransform = { _, _ -> androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 200f) }
                                    )
                                    .shadow(6.dp, RoundedCornerShape(percent = 50), ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.1f))
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
                            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                            exit = scaleOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeOut()
                        ) {
                            val innerVisibilityScope = this
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .shadow(8.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.1f))
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
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) ImmersiveOnGreen else ImmersiveTextSecondary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
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
            enter = expandHorizontally(expandFrom = Alignment.Start, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeOut()
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
