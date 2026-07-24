package com.chenran.parcel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable

// M3 字体比例基线（PRD §5.2）
val BaseTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    // 取件码语义位（等宽 Black）：全 App 唯一取件码样式，经 getPickupCodeTextStyle 读取
    displaySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    ),
)

// 关怀模式：在普通模式基础上按统一倍率（×1.2 ~ ×1.3）放大，
// 保证一切元素切换后相对层级不变（设计规范整改方案 §2.1）。
val SeniorTypography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontSize = 36.sp, lineHeight = 44.sp),
    displayMedium = BaseTypography.displayMedium.copy(fontSize = 32.sp, lineHeight = 40.sp),
    displaySmall = BaseTypography.displaySmall.copy(fontSize = 26.sp, lineHeight = 34.sp), // 取件码 ×1.3
    headlineLarge = BaseTypography.headlineLarge.copy(fontSize = 26.sp, lineHeight = 34.sp),
    headlineMedium = BaseTypography.headlineMedium.copy(fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall = BaseTypography.headlineSmall.copy(fontSize = 22.sp, lineHeight = 30.sp),
    titleLarge = BaseTypography.titleLarge.copy(fontSize = 24.sp, lineHeight = 32.sp),      // ×1.2
    titleMedium = BaseTypography.titleMedium.copy(fontSize = 20.sp, lineHeight = 28.sp),    // ×1.25
    titleSmall = BaseTypography.titleSmall.copy(fontSize = 17.sp, lineHeight = 24.sp),      // ×1.21
    bodyLarge = BaseTypography.bodyLarge.copy(fontSize = 20.sp, lineHeight = 30.sp),        // ×1.25
    bodyMedium = BaseTypography.bodyMedium.copy(fontSize = 17.sp, lineHeight = 26.sp),      // ×1.21
    bodySmall = BaseTypography.bodySmall.copy(fontSize = 15.sp, lineHeight = 22.sp),        // ×1.25
    labelLarge = BaseTypography.labelLarge.copy(fontSize = 17.sp, lineHeight = 24.sp),      // ×1.21
    labelMedium = BaseTypography.labelMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),    // ×1.25
    labelSmall = BaseTypography.labelSmall.copy(fontSize = 14.sp, lineHeight = 20.sp),      // ×1.27
)

@Composable
fun getTypography(isSeniorMode: Boolean): Typography {
    return if (isSeniorMode) SeniorTypography else BaseTypography
}

// ---------------------------------------------------------------
// 取件码样式（设计规范整改后）：统一读取 Typography.displaySmall 语义位，
// 普通 20sp / 关怀 26sp（×1.3），等宽 Black，数字是列表中的首要视觉锚点。
// ---------------------------------------------------------------
@Composable
fun getPickupCodeTextStyle(isSeniorMode: Boolean): TextStyle {
    return MaterialTheme.typography.displaySmall
}
