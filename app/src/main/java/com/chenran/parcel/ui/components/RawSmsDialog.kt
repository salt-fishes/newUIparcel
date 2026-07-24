package com.chenran.parcel.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette

/**
 * v2.0 短信原文弹窗（R11，对齐 Web：标题 + 等宽字体滚动文本 + 关闭按钮）。
 */
@Composable
fun RawSmsDialog(
    body: String,
    onDismiss: () -> Unit,
) {
    val palette = parcelPalette
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.panel,
        title = {
            Text(
                text = "短信完整原文",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
            )
        },
        text = {
            ThemedChip {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = palette.textSecondary,
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(ParcelSpacing.md),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "关闭",
                    color = palette.emerald,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
    )
}
