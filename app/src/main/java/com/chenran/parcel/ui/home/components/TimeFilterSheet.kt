package com.chenran.parcel.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette

/**
 * 时间范围筛选底部弹层：标准列表行（48dp+ 触控），选中项 primary 强调。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFilterSheet(
    sheetState: SheetState,
    options: List<String>,
    selectedIndex: Int,
    isSeniorMode: Boolean,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = parcelPalette
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.panel,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = ParcelSpacing.md,
                vertical = ParcelSpacing.sm,
            ),
        ) {
            Text(
                text = "选择时间范围",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                modifier = Modifier.padding(bottom = ParcelSpacing.sm),
            )
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) palette.badgeBg(palette.emerald) else Color.Transparent,
                        )
                        .clickable { onSelect(index) }
                        .padding(horizontal = ParcelSpacing.sm),
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) palette.emerald else palette.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = palette.emerald,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(ParcelSpacing.md))
        }
    }
}
