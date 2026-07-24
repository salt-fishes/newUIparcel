package com.chenran.parcel.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette

/**
 * 空状态（v2.0）：可选大图标盒 + 主文案 + 引导说明 + 可选动作。
 * 文案需说明"这里会有什么"以及"如何获得"，不重复用户已知信息。
 */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    emoji: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val palette = parcelPalette
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ParcelSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (emoji != null) {
            IconBox(color = palette.emerald, size = 72.dp, cornerRadius = 24.dp) {
                Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(modifier = Modifier.height(ParcelSpacing.md))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = palette.textPrimary,
            textAlign = TextAlign.Center,
        )
        if (description != null) {
            Spacer(modifier = Modifier.height(ParcelSpacing.sm))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        if (action != null) {
            Spacer(modifier = Modifier.height(ParcelSpacing.lg))
            action()
        }
    }
}
