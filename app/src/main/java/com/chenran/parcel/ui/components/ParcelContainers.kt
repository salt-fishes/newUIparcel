package com.chenran.parcel.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chenran.parcel.ui.theme.parcelPalette

/**
 * 标准容器组件（签名与 v1 完全一致，调用方零改动）。
 * v2.0 起视觉实现改为纯色主题化面板族（Surface.kt），色值取自 LocalParcelPalette。
 */

/**
 * 标准卡片：主题 card 色 + outline 细边框（v1 为 surfaceContainerLow 无描边）。
 * 内边距沿用 16h/14v，保证既有布局不变。
 */
@Composable
fun ParcelCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    ThemedCard(modifier = modifier, cornerRadius = cornerRadius, content = content)
}

/**
 * 标准色块/可点击面：默认主题 chip 色。
 * borderAlpha > 0 时使用主题 border 色细描边（兼容旧选中态用法）。
 */
@Composable
fun ParcelSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = Color.Unspecified,
    borderAlpha: Float = 0f,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    ThemedChip(
        modifier = modifier,
        shape = shape,
        color = color,
        bordered = borderAlpha > 0f,
        onClick = onClick,
        enabled = enabled,
        content = content,
    )
}

/**
 * 标准小色块（Chip 容器）：主题 chip 色 + 12dp 圆角。
 */
@Composable
fun ParcelChip(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ThemedChip(modifier = modifier, shape = RoundedCornerShape(12.dp), content = content)
}
