package com.repea.studytrack.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.repea.studytrack.R

val AppFontFamily = FontFamily(Font(R.font.font))
private val BaseTypography = Typography()

val Typography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = AppFontFamily),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = AppFontFamily),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = AppFontFamily),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = AppFontFamily),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = AppFontFamily),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = AppFontFamily),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = AppFontFamily),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = AppFontFamily),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = AppFontFamily),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = AppFontFamily),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = AppFontFamily),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = AppFontFamily),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = AppFontFamily),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = AppFontFamily)
)
