package com.chenran.parcel.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chenran.parcel.ui.theme.parcelPalette

/**
 * 标准 M3 确认对话框。
 * destructive = true 时确认按钮使用 error 色（删除等破坏性操作）。
 */
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = "取消",
    destructive: Boolean = false,
) {
    val palette = parcelPalette
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = palette.panel,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary,
            )
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    color = if (destructive) {
                        palette.rose
                    } else {
                        palette.emerald
                    },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissLabel, color = palette.textSecondary)
            }
        },
    )
}
