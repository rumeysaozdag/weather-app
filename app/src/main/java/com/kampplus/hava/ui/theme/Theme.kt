package com.kampplus.hava.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Varsayılan görünümde uygulanan Kraliyet Moru & Gece İndigo (Midnight Violet) tema.
 * Beyaz tema yerine her durumda referans görseldeki mor/indigo atmosferi korur.
 */
private val LightColors = lightColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VioletPrimaryContainer,
    onPrimaryContainer = VioletTextPrimary,
    secondary = VioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = VioletSecondaryContainer,
    onSecondaryContainer = VioletTextPrimary,
    tertiary = VioletTertiary,
    onTertiary = Color.White,
    tertiaryContainer = VioletTertiaryContainer,
    onTertiaryContainer = VioletTextPrimary,
    background = VioletBackground,
    onBackground = VioletTextPrimary,
    surface = VioletSurface,
    onSurface = VioletTextPrimary,
    surfaceVariant = VioletSurfaceVariant,
    onSurfaceVariant = VioletTextSecondary,
    surfaceContainer = VioletSurfaceContainer,
    surfaceContainerHigh = VioletSurfaceVariant,
    outline = VioletBorderStrong,
    outlineVariant = VioletBorder,
    error = VioletError,
    onError = Color.White,
    errorContainer = VioletErrorContainer,
    onErrorContainer = VioletTextPrimary
)

/**
 * Alternatif koyu görünümde uygulanan Derin Kozmik Mor paleti.
 */
private val DarkColors = darkColorScheme(
    primary = DeepVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = DeepVioletPrimaryContainer,
    onPrimaryContainer = DeepVioletTextPrimary,
    secondary = DeepVioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = DeepVioletSecondaryContainer,
    onSecondaryContainer = DeepVioletTextPrimary,
    tertiary = DeepVioletTertiary,
    onTertiary = Color.White,
    tertiaryContainer = DeepVioletTertiaryContainer,
    onTertiaryContainer = DeepVioletTextPrimary,
    background = DeepVioletBackground,
    onBackground = DeepVioletTextPrimary,
    surface = DeepVioletSurface,
    onSurface = DeepVioletTextPrimary,
    surfaceVariant = DeepVioletSurfaceVariant,
    onSurfaceVariant = DeepVioletTextSecondary,
    surfaceContainer = DeepVioletSurfaceContainer,
    surfaceContainerHigh = DeepVioletSurfaceVariant,
    outline = DeepVioletBorderStrong,
    outlineVariant = DeepVioletBorder,
    error = DeepVioletError,
    onError = Color.White,
    errorContainer = DeepVioletErrorContainer,
    onErrorContainer = DeepVioletTextPrimary
)

/**
 * Uygulamanın teması. Bütün ekranlar bunun içine sarmalanır.
 */
@Composable
fun HavaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = HavaTypography,
        content = content
    )
}
