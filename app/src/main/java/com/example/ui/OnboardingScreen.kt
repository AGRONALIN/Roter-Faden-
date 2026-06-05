package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: AppViewModel,
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    val currentBg = ImmersiveBackground
    val textPrimary = ImmersiveTextPrimary
    val textSecondary = ImmersiveTextSecondary
    val accentRed = ImmersiveGreen
    val pillBg = ImmersivePillBg
    val creamRedColor = CreamRed

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentBg)
    ) {
        // App Name Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentRed)
                )
                Text(
                    text = "Roter Faden",
                    color = textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            if (pagerState.currentPage < 2) {
                Text(
                    text = "Überspringen",
                    color = textSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .bounceClick { onFinish() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Horizontal Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, bottom = 100.dp)
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val alphaScale = (1f - Math.abs(pageOffset).coerceIn(0f, 1f))
            
            val graphicsModifier = Modifier.graphicsLayer {
                val scale = 0.9f + (alphaScale * 0.1f)
                scaleX = scale
                scaleY = scale
                alpha = alphaScale
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(graphicsModifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Visual Slide Area (Uses exactly the real design components of the app)
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (page) {
                        0 -> OnboardingArgumentSlide(accentRed, creamRedColor, textPrimary, textSecondary)
                        1 -> OnboardingGlossarySlide(pillBg, textPrimary, textSecondary)
                        2 -> OnboardingLiteratureSlide(pillBg, accentRed, textPrimary, textSecondary)
                    }
                }

                // Short, punchy Text Area
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    val (title, description) = when (page) {
                        0 -> "Strukturierte Argumente" to "Bringe Klarheit in deine Ausführungen und halte Thesen sowie Gegenargumente geordnet fest."
                        1 -> "Präzises Glossar" to "Definiere und sammle Schlüsselbegriffe an einem organisierten Ort für schnelle Erklärungen."
                        else -> "Zentrale Quellen" to "Verknüpfe deine wissenschaftlichen Quellen und Zitate mit deinen erstellten Diskursen."
                    }

                    Text(
                        text = title,
                        color = textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = description,
                        color = textSecondary,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // Indicators & Control Buttons Row
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 20.dp else 8.dp,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
                        label = "dot_width"
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) accentRed else textSecondary.copy(alpha = 0.3f),
                        animationSpec = tween(200),
                        label = "dot_color"
                    )

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // CTA Button
            val isLastPage = pagerState.currentPage == 2
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isLastPage) accentRed else textPrimary)
                    .bounceClick {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLastPage) "Loslegen" else "Weiter",
                        color = if (isLastPage) Color.White else currentBg,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Weiter",
                        tint = if (isLastPage) Color.White else currentBg,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingArgumentSlide(
    accentRed: Color,
    creamRedColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.9f),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Real Category Badge design element
        Row {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(accentRed.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Ökonomie",
                    color = textPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Real Argument Card design mimicking the actual layout in the app
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(creamRedColor, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Der freie Markt reguliert sich selbst optimal.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Historische Krisen und Ungleichheit belegen die Notwendigkeit von Regulierung.",
                fontSize = 14.sp,
                color = textSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun OnboardingGlossarySlide(
    pillBg: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        // Real Glossary Card design mimicking the exact structure in the app
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(pillBg, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Dialektik",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Ein philosophisches Konzept, bei dem Widersprüche als Triebkraft der Entwicklung verstanden werden.",
                fontSize = 14.sp,
                color = textSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun OnboardingLiteratureSlide(
    pillBg: Color,
    accentRed: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        // Real Literature Card exact design representation
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(pillBg, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Das Kapital",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        lineHeight = 24.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Karl Marx",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentRed,
                        letterSpacing = 1.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = accentRed,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Die Kritik der politischen Ökonomie liefert das fundierte Fundament für gesellschaftliche und politische Analysen.",
                fontSize = 14.sp,
                color = textSecondary,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
