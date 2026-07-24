package com.chenran.parcel.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chenran.parcel.ui.theme.parcelPalette

/**
 * v2.0 彩色图标盒（R11，对齐 Web：bg-{c}-500/20 + border-{c}-500/30 + 16dp 圆角）。
 * 设置列表、卡片头、弹窗头统一使用。
 */
@Composable
fun IconBox(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = parcelPalette
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(cornerRadius),
        color = palette.badgeBg(color),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.badgeBorder(color)),
    ) {
        Box(contentAlignment = Alignment.Center, content = content)
    }
}
