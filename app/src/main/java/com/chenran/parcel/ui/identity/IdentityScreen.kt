package com.chenran.parcel.ui.identity

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chenran.parcel.ui.components.ThemedCard
import com.chenran.parcel.ui.components.ThemedChip
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.Slate500
import com.chenran.parcel.ui.theme.Slate900
import com.chenran.parcel.ui.theme.parcelPalette
import com.chenran.parcel.util.openPddIdentityEntry
import com.chenran.parcel.util.openTaobaoIdentityEntry

private enum class IdentityPlatform(val label: String, val emoji: String, val appName: String) {
    TAOBAO("淘宝 / 菜鸟身份码", "🛒", "淘宝 / 菜鸟"),
    PDD("拼多多身份提货码", "🔴", "拼多多"),
}

/**
 * v2.0 身份码整页（R10）：Web 版布局（顶部横幅 / 平台切换 / App 跳转横幅 / 柜前高亮 / 提示卡），
 * 跳转一律使用 Android 真实 Scheme（IdentityEntries），不做 Web 版演示假码（决策 D7）。
 */
@Composable
fun IdentityScreen(
    context: Context = LocalContext.current,
    isSeniorMode: Boolean = false,
) {
    val palette = parcelPalette
    var platform by remember { mutableStateOf(IdentityPlatform.TAOBAO) }
    var highlightMode by remember { mutableStateOf(false) }

    // 柜前高亮：整页白底高对比（对齐 Web Sun 模式）
    val pageText = if (highlightMode) Slate900 else palette.textPrimary
    val pageTextSub = if (highlightMode) Slate500 else palette.textSecondary

    fun openPlatform() {
        when (platform) {
            IdentityPlatform.TAOBAO -> openTaobaoIdentityEntry(context)
            IdentityPlatform.PDD -> openPddIdentityEntry(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (highlightMode) Modifier.background(Color.White) else Modifier)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ParcelSpacing.md, vertical = ParcelSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ParcelSpacing.md),
    ) {
        // 1. 顶部横幅
        ThemedCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(listOf(palette.fabStart, palette.fabEnd)),
                            RoundedCornerShape(16.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = palette.onFab,
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = ParcelSpacing.sm),
                ) {
                    Text(
                        text = "快递自提身份码与 APP 一键直达",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = pageText,
                    )
                    Text(
                        text = "跳转原生 App 出示真实身份码，取件更快",
                        style = MaterialTheme.typography.bodySmall,
                        color = pageTextSub,
                    )
                }
                IconButton(onClick = { highlightMode = !highlightMode }) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = if (highlightMode) "关闭柜前高亮" else "开启柜前高亮",
                        tint = if (highlightMode) palette.amber else pageTextSub,
                    )
                }
            }
        }

        // 2. App 跳转横幅（橙色渐变卡）；关怀模式改为上下结构，按钮独占一行避免挤压
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                palette.orange.copy(alpha = 0.35f),
                                palette.purple.copy(alpha = 0.25f),
                            ),
                        ),
                    )
                    .padding(ParcelSpacing.md),
                verticalArrangement = Arrangement.spacedBy(ParcelSpacing.sm),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = platform.emoji, style = MaterialTheme.typography.headlineSmall)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = ParcelSpacing.sm),
                    ) {
                        Text(
                            text = "跳转至原生 ${platform.appName} APP 扫码取件",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = pageText,
                        )
                        ThemedChip(bordered = true) {
                            Text(
                                text = "App Scheme 唤起",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.orange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                    if (!isSeniorMode) {
                        Button(
                            onClick = { openPlatform() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.orange,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text("一键打开", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (isSeniorMode) {
                    Button(
                        onClick = { openPlatform() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.orange,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("一键打开", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. 平台切换胶囊；关怀模式整行一个，防止文字换行
        if (isSeniorMode) {
            Column(verticalArrangement = Arrangement.spacedBy(ParcelSpacing.sm)) {
                IdentityPlatform.entries.forEach { item ->
                    val selected = platform == item
                    ThemedChip(
                        color = if (selected) palette.emerald else Color.Unspecified,
                        onClick = { platform = item },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "${item.emoji} ${item.label}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) palette.onFab else pageText,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(ParcelSpacing.sm)) {
                IdentityPlatform.entries.forEach { item ->
                    val selected = platform == item
                    ThemedChip(
                        color = if (selected) palette.emerald else Color.Unspecified,
                        onClick = { platform = item },
                    ) {
                        Text(
                            text = "${item.emoji} ${item.label}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) palette.onFab else pageText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }

        // 4. 跳转卡（真实身份码由对应 App 出示，本页不绘制演示码）
        ThemedCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ParcelSpacing.sm),
            ) {
                ThemedChip(color = palette.badgeBg(palette.orange)) {
                    Text(
                        text = platform.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.orange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    tint = pageTextSub,
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = "点击下方按钮跳转 ${platform.appName}，\n在对应 App 内出示您的真实身份码完成取件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = pageTextSub,
                )
                Button(
                    onClick = { openPlatform() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.emerald,
                        contentColor = palette.onFab,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                    )
                    Text(
                        text = "打开${platform.appName}身份码",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
                Text(
                    text = "未安装对应 App 时将尝试系统浏览器打开",
                    style = MaterialTheme.typography.labelSmall,
                    color = pageTextSub,
                )
            }
        }

        // 5. 底部提示卡
        ThemedCard {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = palette.sky,
                )
                Column(modifier = Modifier.padding(start = ParcelSpacing.sm)) {
                    Text(
                        text = "离线出示小贴士",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = pageText,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "· 柜前可开启右上角「柜前高亮」，提升扫码成功率\n· 屏幕距扫码镜头 15-20cm 并保持静止\n· 网络不佳时可提前打开对应 App 加载身份码",
                        style = MaterialTheme.typography.bodySmall,
                        color = pageTextSub,
                    )
                }
            }
        }

        // 底部预留加大间距，避免内容被全局 FAB（录入短信）遮挡
        Spacer(modifier = Modifier.height(if (isSeniorMode) 120.dp else 88.dp))
    }
}
