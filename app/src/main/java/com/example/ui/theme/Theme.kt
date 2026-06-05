package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+ (disabled by default to prevent wallpaper color clashes in our specific themed app)
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val targetColors = if (darkTheme) darkColors else lightColors

  val immersiveGreen by animateColorAsState(targetColors.immersiveGreen, animationSpec = tween(400), label = "immersiveGreen")
  val immersiveGreenContainer by animateColorAsState(targetColors.immersiveGreenContainer, animationSpec = tween(400), label = "immersiveGreenContainer")
  val creamRed by animateColorAsState(targetColors.creamRed, animationSpec = tween(400), label = "creamRed")
  val creamBlue by animateColorAsState(targetColors.creamBlue, animationSpec = tween(400), label = "creamBlue")
  val immersiveBackground by animateColorAsState(targetColors.immersiveBackground, animationSpec = tween(400), label = "immersiveBackground")
  val immersiveSurface by animateColorAsState(targetColors.immersiveSurface, animationSpec = tween(400), label = "immersiveSurface")
  val immersiveTextPrimary by animateColorAsState(targetColors.immersiveTextPrimary, animationSpec = tween(400), label = "immersiveTextPrimary")
  val immersiveTextSecondary by animateColorAsState(targetColors.immersiveTextSecondary, animationSpec = tween(400), label = "immersiveTextSecondary")
  val immersiveBorder by animateColorAsState(targetColors.immersiveBorder, animationSpec = tween(400), label = "immersiveBorder")
  val immersivePillBg by animateColorAsState(targetColors.immersivePillBg, animationSpec = tween(400), label = "immersivePillBg")
  val immersiveDarkNav by animateColorAsState(targetColors.immersiveDarkNav, animationSpec = tween(400), label = "immersiveDarkNav")
  val immersiveOnGreen by animateColorAsState(targetColors.immersiveOnGreen, animationSpec = tween(400), label = "immersiveOnGreen")
  val immersiveHighlightText by animateColorAsState(targetColors.immersiveHighlightText, animationSpec = tween(400), label = "immersiveHighlightText")

  val appColors = AppColors(
      immersiveGreen = immersiveGreen,
      immersiveGreenContainer = immersiveGreenContainer,
      creamRed = creamRed,
      creamBlue = creamBlue,
      immersiveBackground = immersiveBackground,
      immersiveSurface = immersiveSurface,
      immersiveTextPrimary = immersiveTextPrimary,
      immersiveTextSecondary = immersiveTextSecondary,
      immersiveBorder = immersiveBorder,
      immersivePillBg = immersivePillBg,
      immersiveDarkNav = immersiveDarkNav,
      immersiveOnGreen = immersiveOnGreen,
      immersiveHighlightText = immersiveHighlightText
  )

  val colorScheme = if (darkTheme) {
      darkColorScheme(
          primary = immersiveGreen,
          onPrimary = immersiveOnGreen,
          primaryContainer = immersiveGreenContainer,
          onPrimaryContainer = immersiveHighlightText,
          secondary = PurpleGrey80,
          onSecondary = Color(0xFF333333),
          secondaryContainer = creamRed,
          onSecondaryContainer = immersiveTextPrimary,
          tertiary = creamBlue,
          background = immersiveBackground,
          onBackground = immersiveTextPrimary,
          surface = immersiveSurface,
          onSurface = immersiveTextPrimary,
          surfaceVariant = immersivePillBg,
          onSurfaceVariant = immersiveTextSecondary,
          outline = immersiveBorder
      )
  } else {
      lightColorScheme(
          primary = immersiveGreen,
          onPrimary = immersiveOnGreen,
          primaryContainer = immersiveGreenContainer,
          onPrimaryContainer = immersiveHighlightText,
          secondary = PurpleGrey40,
          onSecondary = Color(0xFFFFFFFF),
          secondaryContainer = creamRed,
          onSecondaryContainer = immersiveTextPrimary,
          tertiary = creamBlue,
          background = immersiveBackground,
          onBackground = immersiveTextPrimary,
          surface = immersiveSurface,
          onSurface = immersiveTextPrimary,
          surfaceVariant = immersivePillBg,
          onSurfaceVariant = immersiveTextSecondary,
          outline = immersiveBorder
      )
  }

  androidx.compose.runtime.CompositionLocalProvider(LocalAppColors provides appColors) {
      MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
