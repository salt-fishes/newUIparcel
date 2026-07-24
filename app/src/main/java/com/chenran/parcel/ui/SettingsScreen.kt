package com.chenran.parcel.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chenran.parcel.ui.components.IconBox
import com.chenran.parcel.ui.components.ThemedCard
import com.chenran.parcel.ui.components.ThemedChip
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette
import com.chenran.parcel.viewmodel.ParcelViewModel
import com.chenran.parcel.util.getAiConfig

private data class SettingsMenuItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val accentKey: String,
    val route: String?,
    val badge: Int = 0,
)

/**
 * v2.0 设置与功能 Tab（R12，对齐 Web SettingsView）：
 * 标题卡（琥珀图标盒 + 系统设置 + 版本徽章）+ 彩色图标盒菜单列表。
 * AI 助手配置菜单项在 R13 加入。
 */
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ParcelViewModel,
    isSeniorMode: Boolean,
    onOpenThemeBg: () -> Unit,
) {
    val palette = parcelPalette
    val context = LocalContext.current
    val failedData by viewModel.failedMessages.collectAsState()
    // AI 配置异步读取，避免组合时同步读盘
    val aiEnabled by produceState(initialValue = false) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            getAiConfig(context).enabled
        }
    }

    val versionName = rememberVersionName()

    val menuItems = listOf(
        SettingsMenuItem("✨", "AI 助手配置", if (aiEnabled) "已启用 · HTTPS 大模型解析" else "未启用 · 手动录入时可预识别", "amber", "ai_settings"),
        SettingsMenuItem("🎨", "界面配色与主题壁纸", "4 套主题、关怀模式与背景图", "emerald", null),
        SettingsMenuItem("📐", "短信解析与正则规则库", "地址/取件码/柜号/屏蔽词规则", "sky", "rules"),
        SettingsMenuItem("🏷️", "地址归类与多标签管理", "多个地址合并为同一标签", "purple", "address_group"),
        SettingsMenuItem("🎚️", "通用界面与显示偏好", "布局、排序、时间与关怀模式", "indigo", "preferences"),
        SettingsMenuItem("🚨", "异常未提取短信排查", "查看解析失败的短信原文", "rose", "fail_sms", failedData.size),
        SettingsMenuItem("🔔", "通知监听权限", "监听第三方 App 通知自动取件", "orange", "use_notification"),
        SettingsMenuItem("📄", "系统运行日志", "短信解析与识别记录", "blue", "logs"),
        SettingsMenuItem("💾", "本地数据备份与导出", "整站配置与规则 JSON", "emerald", "backup"),
        SettingsMenuItem("ℹ️", "关于包裹助手", "版本与开源信息", "gray", "about"),
    )

    fun accentOf(key: String): Color = when (key) {
        "emerald" -> palette.emerald
        "amber" -> palette.amber
        "sky" -> palette.sky
        "purple" -> palette.purple
        "indigo" -> palette.indigo
        "rose" -> palette.rose
        "orange" -> palette.orange
        "blue" -> palette.blue
        else -> palette.textSecondary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ParcelSpacing.md, vertical = ParcelSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ParcelSpacing.sm),
    ) {
        // 标题卡（Web：Settings 琥珀盒 + 系统设置 + 版本徽章）
        ThemedCard(cornerRadius = 24.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBox(color = palette.amber, size = 48.dp, cornerRadius = 16.dp) {
                    Text(text = "⚙️", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.width(ParcelSpacing.sm))
                Text(
                    text = "系统设置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                ThemedChip(color = palette.badgeBg(palette.amber)) {
                    Text(
                        text = "v$versionName",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.amber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        // 菜单项
        menuItems.forEach { item ->
            val accent = accentOf(item.accentKey)
            ThemedCard(
                cornerRadius = 16.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (item.route == null) {
                                onOpenThemeBg()
                            } else {
                                navController.navigate(item.route)
                            }
                        },
                ) {
                    IconBox(color = accent, size = 44.dp, cornerRadius = 14.dp) {
                        Text(text = item.emoji, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.width(ParcelSpacing.sm))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .then(Modifier),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary,
                            )
                            if (item.badge > 0) {
                                Spacer(modifier = Modifier.width(ParcelSpacing.xs))
                                ThemedChip(color = palette.badgeBg(palette.rose)) {
                                    Text(
                                        text = "${item.badge} 条",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.rose,
                                        modifier = Modifier.padding(horizontal = ParcelSpacing.xs, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                        )
                    }
                    IconButtonWrapper(
                        onClick = {
                            if (item.route == null) {
                                onOpenThemeBg()
                            } else {
                                navController.navigate(item.route)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = "打开${item.title}",
                            tint = palette.textSecondary,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.padding(bottom = ParcelSpacing.xxl))
    }
}

@Composable
private fun IconButtonWrapper(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.IconButton(onClick = onClick) {
        content()
    }
}

@Composable
private fun rememberVersionName(): String {
    val context = LocalContext.current
    return androidx.compose.runtime.remember {
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrDefault("1.0.7") ?: "1.0.7"
    }
}
