package com.chenran.parcel.ui.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.chenran.parcel.ui.components.ThemedCard
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette

/**
 * v2.0 通用界面与显示偏好（R12，对齐 Web preferences 子视图：4 个开关行卡片）。
 */
@Composable
fun PreferencesScreen(
    navController: NavController,
    sortByLocker: Boolean,
    isHorizontalLayout: Boolean,
    showCodeTime: Boolean,
    isSeniorMode: Boolean,
    onToggleSort: (Boolean) -> Unit,
    onToggleLayout: (Boolean) -> Unit,
    onToggleTime: (Boolean) -> Unit,
    onToggleSenior: (Boolean) -> Unit,
) {
    val palette = parcelPalette
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ParcelSpacing.md, vertical = ParcelSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ParcelSpacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = palette.textPrimary,
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "通用界面与显示偏好",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
            )
        }

        PreferenceSwitchRow(
            title = "横向卡片 Tab 视图",
            subtitle = "地址分组以横向标签页展示",
            checked = isHorizontalLayout,
            onCheckedChange = onToggleLayout,
        )
        PreferenceSwitchRow(
            title = "按智能柜格口排序",
            subtitle = "按柜号数字升序排列",
            checked = sortByLocker,
            onCheckedChange = onToggleSort,
        )
        PreferenceSwitchRow(
            title = "显示收到短信时间节点",
            subtitle = "在取件码右侧显示短信时间",
            checked = showCodeTime,
            onCheckedChange = onToggleTime,
        )
        PreferenceSwitchRow(
            title = "长辈关怀高对比大字模式",
            subtitle = "更大字号与触控目标",
            checked = isSeniorMode,
            onCheckedChange = onToggleSenior,
        )
    }
}

@Composable
private fun PreferenceSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val palette = parcelPalette
    ThemedCard(cornerRadius = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
