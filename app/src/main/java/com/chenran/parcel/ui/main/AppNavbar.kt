package com.chenran.parcel.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.chenran.parcel.ui.components.IconBox
import com.chenran.parcel.ui.components.ThemedChip
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette

/**
 * v2.0 全局顶部导航栏（R11，对齐 Web Navbar）：
 * 品牌（emerald 图标盒 + 标题 + 关怀徽章）+ 关怀开关 + 主题入口 + 视图偏好下拉。
 * 仅在 4 个 Tab 根路由显示（由 MainScaffold 控制）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavbar(
    isSeniorMode: Boolean,
    onToggleSenior: () -> Unit,
    sortByLocker: Boolean,
    isHorizontalLayout: Boolean,
    showCodeTime: Boolean,
    onToggleSort: () -> Unit,
    onToggleLayout: () -> Unit,
    onToggleTime: () -> Unit,
    onOpenTheme: () -> Unit,
) {
    val palette = parcelPalette
    var showViewMenu by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = palette.panel,
            titleContentColor = palette.textPrimary,
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBox(color = palette.emerald, size = 36.dp, cornerRadius = 12.dp) {
                    Text(text = "📦", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.width(ParcelSpacing.sm))
                Text(
                    text = "包裹取件码",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    color = palette.textPrimary,
                )
                if (isSeniorMode) {
                    Spacer(modifier = Modifier.width(ParcelSpacing.sm))
                    ThemedChip(color = palette.badgeBg(palette.amber)) {
                        Text(
                            text = "关怀",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.amber,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        },
        actions = {
            // 关怀模式快捷开关（Web Sparkles）
            IconButton(onClick = onToggleSenior) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = if (isSeniorMode) "关闭关怀模式" else "开启关怀模式",
                    tint = if (isSeniorMode) palette.amber else palette.textSecondary,
                )
            }
            // 主题与壁纸入口（Web Palette，R12 承接 ThemeBgSheet）
            IconButton(onClick = onOpenTheme) {
                Text(text = "🎨", style = MaterialTheme.typography.titleMedium)
            }
            // 视图偏好下拉（Web SlidersHorizontal）
            Box {
                IconButton(onClick = { showViewMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "视图偏好",
                        tint = palette.textSecondary,
                    )
                }
                DropdownMenu(
                    expanded = showViewMenu,
                    onDismissRequest = { showViewMenu = false },
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(if (sortByLocker) "取消按柜号排序" else "按柜号排序")
                        },
                        onClick = {
                            showViewMenu = false
                            onToggleSort()
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(if (isHorizontalLayout) "切为纵向列表" else "切为横向标签")
                        },
                        onClick = {
                            showViewMenu = false
                            onToggleLayout()
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(if (showCodeTime) "隐藏短信时间" else "显示短信时间")
                        },
                        onClick = {
                            showViewMenu = false
                            onToggleTime()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("外观与背景壁纸…") },
                        onClick = {
                            showViewMenu = false
                            onOpenTheme()
                        },
                    )
                }
            }
        },
    )
}
