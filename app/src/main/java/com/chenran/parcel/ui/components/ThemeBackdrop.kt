package com.chenran.parcel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.chenran.parcel.ui.theme.parcelPalette

/**
 * v2.0 主题渐变背景层（R9）：4 套主题各一条对角渐变（主题标识，对齐 Web）。
 * 置于 NavHost 之下；壁纸开启时由 WallpaperBackdrop 覆盖。
 */
@Composable
fun ThemeBackdrop(modifier: Modifier = Modifier) {
    val palette = parcelPalette
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        palette.pageGradientStart,
                        palette.pageGradientMid,
                        palette.pageGradientEnd,
                    ),
                ),
            ),
    )
}
