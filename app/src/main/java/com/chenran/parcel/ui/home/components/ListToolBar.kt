package com.chenran.parcel.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chenran.parcel.ui.components.ThemedCard
import com.chenran.parcel.ui.components.ThemedChip
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette

/**
 * v2.0 列表工具条（R11，对齐 Web）：左时间筛选，右成功/排查计数徽章。
 * 「导出图片卡片」按钮在 R14 接入。
 */
@Composable
fun ListToolBar(
    timeFilterLabel: String,
    onTimeFilterClick: () -> Unit,
    successCount: Int,
    failCount: Int,
    onSuccessClick: () -> Unit,
    onFailClick: () -> Unit,
    onExportClick: () -> Unit,
    isSeniorMode: Boolean,
) {
    val palette = parcelPalette
    ThemedCard(cornerRadius = 16.dp, modifier = Modifier.padding(horizontal = ParcelSpacing.md)) {
        if (isSeniorMode) {
            // 关怀模式：大字号下单行放不下，拆成“筛选+导出 / 计数徽章”两行
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ParcelSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TimeFilterChip(timeFilterLabel, onTimeFilterClick)
                    ExportChip(onExportClick)
                }
                if (successCount > 0 || failCount > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(ParcelSpacing.sm)) {
                        if (successCount > 0) {
                            CountChip("成功 $successCount", palette.emerald, Icons.Outlined.Info, onSuccessClick)
                        }
                        if (failCount > 0) {
                            CountChip("排查 $failCount", palette.rose, Icons.Outlined.Warning, onFailClick)
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // 左：时间筛选（Web Calendar 图标 emerald + 当前筛选名）
                TimeFilterChip(timeFilterLabel, onTimeFilterClick)

                // 右：导出图片卡片 + 成功/排查计数徽章
                Row(horizontalArrangement = Arrangement.spacedBy(ParcelSpacing.sm)) {
                    ExportChip(onExportClick)
                    if (successCount > 0) {
                        CountChip("成功 $successCount", palette.emerald, Icons.Outlined.Info, onSuccessClick)
                    }
                    if (failCount > 0) {
                        CountChip("排查 $failCount", palette.rose, Icons.Outlined.Warning, onFailClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeFilterChip(label: String, onClick: () -> Unit) {
    val palette = parcelPalette
    ThemedChip(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.DateRange,
                contentDescription = null,
                tint = palette.emerald,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
private fun ExportChip(onClick: () -> Unit) {
    val palette = parcelPalette
    ThemedChip(color = palette.badgeBg(palette.emerald), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Icon(Icons.Outlined.Image, contentDescription = "导出图片卡片", tint = palette.emerald)
            Text(
                text = "导出",
                style = MaterialTheme.typography.labelLarge,
                color = palette.emerald,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun CountChip(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val palette = parcelPalette
    ThemedChip(color = palette.badgeBg(color), onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}
