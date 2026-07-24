package com.chenran.parcel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chenran.parcel.util.WallpaperSettings

/**
 * 主页壁纸背景层：
 * - 低透明度呈现：壁纸上叠加背景色 scrim，保证文字对比度接近纯 surface；
 * - 自动压暗：额外叠加 overlayAlpha 黑色遮罩，深色/浅色下均可读；
 * - v2.0 对齐 Web：支持模糊、构图偏移、亮度/对比度（ColorMatrix）与 Cover/Contain。
 */
@Composable
fun WallpaperBackdrop(
    wallpaperUri: String,
    settings: WallpaperSettings,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    // scrim 透明度：gradientAlpha 越低遮罩越重；下限 0.55 保证 ≥4.5:1 对比度
    val scrimAlpha = (1f - settings.gradientAlpha).coerceIn(0.55f, 0.92f)
    val scrimColor = MaterialTheme.colorScheme.background

    // 亮度/对比度矩阵：out = (in - 0.5) * contrast + 0.5，再乘 brightness
    val brightnessFactor = settings.brightness / 100f
    val contrastFactor = settings.contrast / 100f
    val scale = brightnessFactor * contrastFactor
    val translate = (0.5f - 0.5f * contrastFactor) * 255f * brightnessFactor
    val colorMatrix = ColorMatrix(
        floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f,
        ),
    )

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(wallpaperUri)
                .build(),
            contentDescription = null,
            contentScale = if (settings.fitContain) ContentScale.Fit else ContentScale.Crop,
            alignment = BiasAlignment(settings.alignmentX, settings.alignmentY),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(colorMatrix),
            modifier = Modifier
                .fillMaxSize()
                .then(if (settings.blurRadius > 0f) Modifier.blur(settings.blurRadius.dp) else Modifier),
        )
        // 压暗层：保证亮色壁纸上的深色文字可读
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = settings.overlayAlpha)),
        )
        // scrim 层：整体拉回主题背景色，保证对比度
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor.copy(alpha = scrimAlpha)),
        )
    }
}
