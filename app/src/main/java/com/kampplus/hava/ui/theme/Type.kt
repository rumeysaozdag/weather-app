package com.kampplus.hava.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Notion tarzı editoryal tipografi ölçeği.
 *
 * Net başlıklar, sıkı harf aralıkları (letterSpacing) ve okunaklı gövde metinleri.
 */
private val base = Typography()

internal val HavaTypography = base.copy(
    displayLarge = base.displayLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1.2).sp
    ),
    displayMedium = base.displayMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.8).sp
    ),
    displaySmall = base.displaySmall.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = base.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp
    ),
    headlineSmall = base.headlineSmall.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = base.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = base.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.1).sp
    ),
    titleSmall = base.titleSmall.copy(
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = base.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp
    ),
    bodyMedium = base.bodyMedium.copy(
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    ),
    labelLarge = base.labelLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp
    ),
    labelMedium = base.labelMedium.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp
    ),
    labelSmall = base.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp
    )
)
