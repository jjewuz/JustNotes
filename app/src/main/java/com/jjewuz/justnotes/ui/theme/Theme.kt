package com.jjewuz.justnotes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.jjewuz.justnotes.R


private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

@Composable
fun AppTheme(
    useDynamicColor: Boolean = true,
    useCustomFont: Boolean = true,
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
  val colors = when {
      useDynamicColor && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ->{
          val context = LocalContext.current
          if (useDarkTheme) dynamicDarkColorScheme(context)
          else dynamicLightColorScheme(context)
      }
      useDarkTheme -> DarkColors
      else -> LightColors
  }

    val font = FontFamily(
        Font(R.font.raleway)
    )

    val defaultTypography = Typography()
    val typo = when {
        !useCustomFont ->{
            Typography(
                displayLarge = defaultTypography.displayLarge.copy(fontFamily = font),
                displayMedium = defaultTypography.displayMedium.copy(fontFamily = font),
                displaySmall = defaultTypography.displaySmall.copy(fontFamily = font),

                bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = font),
                bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = font),
                bodySmall = defaultTypography.bodySmall.copy(fontFamily = font),

                headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = font),
                headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = font),
                headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = font),

                titleLarge = defaultTypography.titleLarge.copy(fontFamily = font),
                titleMedium = defaultTypography.titleMedium.copy(fontFamily = font),
                titleSmall = defaultTypography.titleSmall.copy(fontFamily = font),

                labelLarge = defaultTypography.labelLarge.copy(fontFamily = font),
                labelMedium = defaultTypography.labelMedium.copy(fontFamily = font),
                labelSmall = defaultTypography.labelSmall.copy(fontFamily = font),
            )
        }
        else -> Typography()
    }

  MaterialTheme(
        colorScheme = colors,
        typography = typo,
        content = content
  )
}