package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavController
import com.example.data.LiteratureItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun LiteratureDetailScreen(
    literatureItem: LiteratureItem,
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sourceKey: String = "card"
) {
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
                        sharedContentState = rememberSharedContentState(key = "literature_${literatureItem.id}_${sourceKey}"),
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
                // Header (Back button)
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

                    Text("☭", color = Color.Red, fontSize = 28.sp)
                }
                
                Text(
                    text = literatureItem.title, 
                    fontWeight = FontWeight.Black,
                    color = ImmersiveTextPrimary,
                    style = MaterialTheme.typography.headlineMedium
                ) 

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = literatureItem.author,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveGreen,
                    style = MaterialTheme.typography.titleMedium,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = literatureItem.summary,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        color = ImmersiveTextSecondary,
                        lineHeight = 28.sp,
                        fontSize = 18.sp
                    )
                )
            
                Spacer(modifier = Modifier.height(120.dp))
            }
            }
        }
    }
}
