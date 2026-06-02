package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

class AppColors(
    val immersiveGreen: Color,
    val immersiveGreenContainer: Color,
    val creamRed: Color,
    val creamBlue: Color,
    val immersiveBackground: Color,
    val immersiveSurface: Color,
    val immersiveTextPrimary: Color,
    val immersiveTextSecondary: Color,
    val immersiveBorder: Color,
    val immersivePillBg: Color,
    val immersiveDarkNav: Color,
    val immersiveOnGreen: Color,
    val immersiveHighlightText: Color
)

val lightColors = AppColors(
    immersiveGreen = Color(0xFFC00020),
    immersiveGreenContainer = Color(0xFFFCDCDA),
    creamRed = Color(0xFFFAE3E1),
    creamBlue = Color(0xFFD3E0EE),
    immersiveBackground = Color(0xFFFBF8F6),
    immersiveSurface = Color(0xFFFFFFFF),
    immersiveTextPrimary = Color(0xFF1F1A1A),
    immersiveTextSecondary = Color(0xFF534343),
    immersiveBorder = Color(0xFFE8DFDD),
    immersivePillBg = Color(0xFFF5EEEE),
    immersiveDarkNav = Color(0xFF362F2E),
    immersiveOnGreen = Color(0xFFFFFFFF),
    immersiveHighlightText = Color(0xFF3D0006)
)

val darkColors = AppColors(
    immersiveGreen = Color(0xFFFF5468),
    immersiveGreenContainer = Color(0xFF3D0006),
    creamRed = Color(0xFF4A1010),
    creamBlue = Color(0xFF152A4A),
    immersiveBackground = Color(0xFF121212),
    immersiveSurface = Color(0xFF1E1E1E),
    immersiveTextPrimary = Color(0xFFEAEAEA),
    immersiveTextSecondary = Color(0xFFA0A0A0),
    immersiveBorder = Color(0xFF333333),
    immersivePillBg = Color(0xFF2A2A2A),
    immersiveDarkNav = Color(0xFF362F2E),
    immersiveOnGreen = Color(0xFFFFFFFF),
    immersiveHighlightText = Color(0xFFFF8998)
)

val LocalAppColors = staticCompositionLocalOf { lightColors }

val ImmersiveGreen: Color @Composable get() = LocalAppColors.current.immersiveGreen
val ImmersiveGreenContainer: Color @Composable get() = LocalAppColors.current.immersiveGreenContainer
val CreamRed: Color @Composable get() = LocalAppColors.current.creamRed
val CreamBlue: Color @Composable get() = LocalAppColors.current.creamBlue
val ImmersiveBackground: Color @Composable get() = LocalAppColors.current.immersiveBackground
val ImmersiveSurface: Color @Composable get() = LocalAppColors.current.immersiveSurface
val ImmersiveTextPrimary: Color @Composable get() = LocalAppColors.current.immersiveTextPrimary
val ImmersiveTextSecondary: Color @Composable get() = LocalAppColors.current.immersiveTextSecondary
val ImmersiveBorder: Color @Composable get() = LocalAppColors.current.immersiveBorder
val ImmersivePillBg: Color @Composable get() = LocalAppColors.current.immersivePillBg
val ImmersiveDarkNav: Color @Composable get() = LocalAppColors.current.immersiveDarkNav
val ImmersiveOnGreen: Color @Composable get() = LocalAppColors.current.immersiveOnGreen
val ImmersiveHighlightText: Color @Composable get() = LocalAppColors.current.immersiveHighlightText

// M3 Palette Compatibility
val Purple80 = Color(0xFFFCDCDA)
val PurpleGrey80 = Color(0xFFF5EEEE)
val Pink80 = Color(0xFFEADFDF)

val Purple40 = Color(0xFFC00020)
val PurpleGrey40 = Color(0xFF857372)
val Pink40 = Color(0xFFEADFDF)
