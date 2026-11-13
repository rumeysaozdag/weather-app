package com.kampplus.hava.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Havadaş için referans görseldeki Gece İndigo & Kraliyet Moru (Midnight Violet)
 * minimalist renk paleti.
 *
 * Minimalist blok/çerçeve yapısı korunurken tüm yüzeyler derin gece moru,
 * kartlar doygun viyole/indigo, vurgu noktaları ise neon eflatun/pembe-mor tonlarındadır.
 */

// Ana Gece Moru & İndigo Paleti (Referans Görsel Tonları: #1C1B33 -> #2E335A -> #48319D)
internal val VioletBackground = Color(0xFF1C1B33)
internal val VioletSurface = Color(0xFF252247)
internal val VioletSurfaceVariant = Color(0xFF312B5E)
internal val VioletSurfaceContainer = Color(0xFF48319D)
internal val VioletBorder = Color(0xFF4E4287)
internal val VioletBorderStrong = Color(0xFF7562B8)
internal val VioletTextPrimary = Color(0xFFFFFFFF)
internal val VioletTextSecondary = Color(0xFFC8C2E4)

internal val VioletPrimary = Color(0xFF8B5CF6)
internal val VioletPrimaryContainer = Color(0xFF48319D)
internal val VioletSecondary = Color(0xFFC427FB)
internal val VioletSecondaryContainer = Color(0xFF3B246B)
internal val VioletTertiary = Color(0xFFE879F9)
internal val VioletTertiaryContainer = Color(0xFF4A2566)
internal val VioletError = Color(0xFFFF6B8B)
internal val VioletErrorContainer = Color(0xFF4F1F34)

// İkincil (Daha Derin Kozmik Mor) Modu — Tema değiştirme tuşu (◐) için uyumlu derin mor tonu
internal val DeepVioletBackground = Color(0xFF141326)
internal val DeepVioletSurface = Color(0xFF1D1A36)
internal val DeepVioletSurfaceVariant = Color(0xFF27224A)
internal val DeepVioletSurfaceContainer = Color(0xFF3B2882)
internal val DeepVioletBorder = Color(0xFF3F3570)
internal val DeepVioletBorderStrong = Color(0xFF63529E)
internal val DeepVioletTextPrimary = Color(0xFFF8F6FF)
internal val DeepVioletTextSecondary = Color(0xFFBDB6DC)

internal val DeepVioletPrimary = Color(0xFFA78BFA)
internal val DeepVioletPrimaryContainer = Color(0xFF3B2882)
internal val DeepVioletSecondary = Color(0xFFD946EF)
internal val DeepVioletSecondaryContainer = Color(0xFF331E5C)
internal val DeepVioletTertiary = Color(0xFFF0ABFC)
internal val DeepVioletTertiaryContainer = Color(0xFF402059)
internal val DeepVioletError = Color(0xFFFF809B)
internal val DeepVioletErrorContainer = Color(0xFF461B2E)

/** Geriye dönük uyumluluk için tutulan arka plan yardımcıları. */
internal val HavaBackgroundColors = listOf(Color(0xFF2E335A), Color(0xFF1C1B33))
internal val HavaBackgroundBrush = Brush.verticalGradient(HavaBackgroundColors)
internal val HavaNavigationColor = VioletSurface
