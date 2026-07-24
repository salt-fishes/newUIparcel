package com.chenran.parcel.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette

/**
 * v2.0 面板组件族（R9，决策 D1：无玻璃，各主题不透明纯色 + 主题 outline 细边框）。
 * 全 App 面板/卡片/芯片视觉的唯一出处，色值统一取自 LocalParcelPalette。
 */

/** 面板：顶栏 / 底栏 / 弹层底色（palette.panel），无边框或细边框。 */
@Composable
fun ThemedPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    bordered: Boolean = false,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = parcelPalette.panel,
        border = if (bordered) BorderStroke(1.dp, parcelPalette.border) else null,
        content = content,
    )
}

/** 卡片：内容分组容器（palette.card + outline 细边框），16h/14v 内边距。 */
@Composable
fun ThemedCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    bordered: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        color = parcelPalette.card,
        border = if (bordered) BorderStroke(1.dp, parcelPalette.border) else null,
    ) {
        Column(
            modifier = Modifier.padding(ParcelSpacing.md),
            content = content,
        )
    }
}

/** 芯片：小色块容器（palette.chip），可点击/可选中（选中时用强调色底）。 */
@Composable
fun ThemedChip(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    color: Color = Color.Unspecified,
    bordered: Boolean = false,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val resolvedColor = if (color != Color.Unspecified) color else parcelPalette.chip
    val border = if (bordered) BorderStroke(1.dp, parcelPalette.border) else null
    if (onClick != null) {
        Surface(
            // 可点击胶囊保证 ≥40dp 触控高度（移动端便捷性）
            modifier = modifier.defaultMinSize(minHeight = 40.dp),
            shape = shape,
            color = resolvedColor,
            border = border,
            onClick = onClick,
            enabled = enabled,
            content = content,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = resolvedColor,
            border = border,
            content = content,
        )
    }
}
