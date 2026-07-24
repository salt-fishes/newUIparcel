package com.chenran.parcel.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chenran.parcel.ui.components.ThemedChip
import com.chenran.parcel.ui.main.LocalAppToast
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette
import com.chenran.parcel.util.BackgroundManager
import com.chenran.parcel.util.ThemeManager
import com.chenran.parcel.util.WallpaperSettings

private data class ThemeOption(
    val mode: Int,
    val emoji: String,
    val label: String,
    val desc: String,
)

/**
 * v2.0 主题与壁纸弹层（R12，对齐 Web ThemeBgModal，吸收 WallpaperAdjustSheet）：
 * Tab1 主题卡网格（4 主题 + 跟随系统）+ 关怀模式开关；Tab2 壁纸与图像微调。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeBgSheet(
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
    isSeniorMode: Boolean,
    onToggleSenior: (Boolean) -> Unit,
    onWallpaperChanged: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = parcelPalette
    val context = LocalContext.current
    val showToast = LocalAppToast.current
    var tabIndex by remember { mutableStateOf(0) }

    var wallpaperEnabled by remember { mutableStateOf(BackgroundManager.isWallpaperEnabled(context)) }
    var wallpaperUri by remember { mutableStateOf(BackgroundManager.getHomeWallpaperUri(context)) }
    var wpSettings by remember { mutableStateOf(BackgroundManager.getWallpaperSettings(context)) }
    var urlInput by remember { mutableStateOf("") }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            // 获取长期读权限，否则重启后壁纸 Uri 失效
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }.onFailure { showToast("壁纸长期授权失败，重启后可能失效") }
            BackgroundManager.setHomeWallpaperUri(context, uri.toString())
            wallpaperUri = uri.toString()
            wallpaperEnabled = true
            onWallpaperChanged()
            showToast("壁纸已更新")
        }
    }

    fun applySettings(new: WallpaperSettings) {
        wpSettings = new
        BackgroundManager.setWallpaperSettings(context, new)
        onWallpaperChanged()
    }

    // 拖动过程中只更新本地预览状态，拖动结束才落盘并触发壁纸重载，避免每帧写盘掉帧
    fun previewSettings(new: WallpaperSettings) {
        wpSettings = new
    }

    val themeOptions = listOf(
        ThemeOption(ThemeManager.MODE_SYSTEM, "📱", "跟随系统", "自动切换明暗"),
        ThemeOption(ThemeManager.MODE_LIGHT, "☀️", "日间明亮", "清爽浅色界面"),
        ThemeOption(ThemeManager.MODE_DARK, "🌙", "经典深色", "护眼暗色界面"),
        ThemeOption(ThemeManager.MODE_EMERALD, "✨", "翡翠夜景", "深绿氛围主题"),
        ThemeOption(ThemeManager.MODE_VIOLET, "🔮", "极光紫罗兰", "紫色氛围主题"),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.panel,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ParcelSpacing.md),
        ) {
            Text(
                text = "界面配色与主题壁纸",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
            )
            Spacer(modifier = Modifier.height(ParcelSpacing.sm))

            // Tab 切换
            Row(horizontalArrangement = Arrangement.spacedBy(ParcelSpacing.sm)) {
                ThemedChip(
                    color = if (tabIndex == 0) palette.emerald else androidx.compose.ui.graphics.Color.Unspecified,
                    onClick = { tabIndex = 0 },
                ) {
                    Text(
                        text = "配色与主题",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (tabIndex == 0) palette.onFab else palette.textPrimary,
                        fontWeight = if (tabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = ParcelSpacing.sm, vertical = ParcelSpacing.sm),
                    )
                }
                ThemedChip(
                    color = if (tabIndex == 1) palette.emerald else androidx.compose.ui.graphics.Color.Unspecified,
                    onClick = { tabIndex = 1 },
                ) {
                    Text(
                        text = "自定义背景图",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (tabIndex == 1) palette.onFab else palette.textPrimary,
                        fontWeight = if (tabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = ParcelSpacing.sm, vertical = ParcelSpacing.sm),
                    )
                }
            }
            Spacer(modifier = Modifier.height(ParcelSpacing.md))

            if (tabIndex == 0) {
                // 主题卡网格（2 列）
                themeOptions.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ParcelSpacing.sm),
                    ) {
                        rowItems.forEach { option ->
                            val selected = themeMode == option.mode
                            Surface(
                                onClick = {
                                    onThemeModeChange(option.mode)
                                    ThemeManager.setThemeMode(context, option.mode)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (selected) palette.badgeBg(palette.emerald) else palette.chip,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) palette.emerald else palette.border,
                                ),
                            ) {
                                Row(
                                    modifier = Modifier.padding(ParcelSpacing.md),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = option.emoji,
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp),
                                    ) {
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = palette.textPrimary,
                                        )
                                        Text(
                                            text = option.desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.textSecondary,
                                        )
                                    }
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = palette.emerald,
                                        )
                                    }
                                }
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // 关怀模式开关（Web 琥珀色）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = ParcelSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "长辈关怀模式",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                        )
                        Text(
                            text = "高对比大字，方便长辈使用",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                        )
                    }
                    Switch(checked = isSeniorMode, onCheckedChange = onToggleSenior)
                }
            } else {
                // 壁纸控制
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "启用主页壁纸",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                        )
                        Text(
                            text = if (wallpaperEnabled) "已开启" else "默认关闭，使用主题渐变背景",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                        )
                    }
                    Switch(
                        checked = wallpaperEnabled,
                        onCheckedChange = { checked ->
                            wallpaperEnabled = checked
                            BackgroundManager.setWallpaperEnabled(context, checked)
                            onWallpaperChanged()
                        },
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = ParcelSpacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(ParcelSpacing.sm),
                ) {
                    OutlinedButton(
                        onClick = { imageLauncher.launch(arrayOf("image/*")) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("从相册选择")
                    }
                    OutlinedButton(
                        onClick = {
                            BackgroundManager.clearHomeWallpaper(context)
                            wallpaperUri = ""
                            wallpaperEnabled = false
                            onWallpaperChanged()
                            showToast("壁纸已清除")
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("清除壁纸")
                    }
                }

                // URL 输入（对齐 Web URL 壁纸）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("图片 URL（https）") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    OutlinedButton(
                        onClick = {
                            val url = urlInput.trim()
                            if (url.startsWith("https://")) {
                                BackgroundManager.setHomeWallpaperUri(context, url)
                                wallpaperUri = url
                                wallpaperEnabled = true
                                onWallpaperChanged()
                                showToast("壁纸已更新")
                            } else {
                                showToast("仅支持 https 图片地址")
                            }
                        },
                    ) {
                        Text("应用")
                    }
                }

                Spacer(modifier = Modifier.height(ParcelSpacing.sm))
                WpSlider(
                    "模糊", wpSettings.blurRadius, 0f, 40f,
                    onChange = { v -> previewSettings(wpSettings.copy(blurRadius = v)) },
                    onChangeFinished = { applySettings(wpSettings) },
                )
                WpSlider(
                    "遮罩", wpSettings.overlayAlpha, 0f, 0.5f,
                    onChange = { v -> previewSettings(wpSettings.copy(overlayAlpha = v)) },
                    onChangeFinished = { applySettings(wpSettings) },
                )
                WpSlider(
                    "渐变透明度", wpSettings.gradientAlpha, 0.1f, 0.8f,
                    onChange = { v -> previewSettings(wpSettings.copy(gradientAlpha = v)) },
                    onChangeFinished = { applySettings(wpSettings) },
                )
                WpSlider(
                    "亮度", wpSettings.brightness, 50f, 150f,
                    onChange = { v -> previewSettings(wpSettings.copy(brightness = v)) },
                    onChangeFinished = { applySettings(wpSettings) },
                )
                WpSlider(
                    "对比度", wpSettings.contrast, 50f, 150f,
                    onChange = { v -> previewSettings(wpSettings.copy(contrast = v)) },
                    onChangeFinished = { applySettings(wpSettings) },
                )

                // Cover / Contain 切换
                Row(horizontalArrangement = Arrangement.spacedBy(ParcelSpacing.sm)) {
                    ThemedChip(
                        color = if (!wpSettings.fitContain) {
                            palette.emerald
                        } else {
                            androidx.compose.ui.graphics.Color.Unspecified
                        },
                        onClick = { applySettings(wpSettings.copy(fitContain = false)) },
                    ) {
                        Text(
                            text = "Cover 铺满",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (!wpSettings.fitContain) palette.onFab else palette.textPrimary,
                            modifier = Modifier.padding(horizontal = ParcelSpacing.sm, vertical = ParcelSpacing.sm),
                        )
                    }
                    ThemedChip(
                        color = if (wpSettings.fitContain) {
                            palette.emerald
                        } else {
                            androidx.compose.ui.graphics.Color.Unspecified
                        },
                        onClick = { applySettings(wpSettings.copy(fitContain = true)) },
                    ) {
                        Text(
                            text = "Contain 完整",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (wpSettings.fitContain) palette.onFab else palette.textPrimary,
                            modifier = Modifier.padding(horizontal = ParcelSpacing.sm, vertical = ParcelSpacing.sm),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ParcelSpacing.md))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.emerald,
                    contentColor = palette.onFab,
                ),
            ) {
                Text("完成并保存外观", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(ParcelSpacing.lg))
        }
    }
}

@Composable
private fun WpSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    onChange: (Float) -> Unit,
    onChangeFinished: () -> Unit = {},
) {
    val palette = parcelPalette
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textPrimary,
            )
            Text(
                text = if (max > 1f) "${value.toInt()}" else "%.2f".format(value),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            onValueChangeFinished = onChangeFinished,
            valueRange = min..max,
        )
    }
}
