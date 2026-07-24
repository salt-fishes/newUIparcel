package com.chenran.parcel.ui.home.components

import android.content.ClipData
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.chenran.parcel.ui.main.LocalAppToast
import com.chenran.parcel.ui.theme.getPickupCodeTextStyle
import com.chenran.parcel.ui.theme.parcelPalette
import com.chenran.parcel.util.formatPickupCode

/**
 * 取件码胶囊（v2.0 R11，对齐 Web）：emerald 徽章底 + mono Black 码 + 复制按钮；
 * 点击整体切换完成态；已取件降级为中性色 + 删除线。
 * 触控目标：默认 ≥48dp，关怀模式 ≥56dp（码区更高对齐 Web 52dp）。
 */
@Composable
fun PickupCodeChip(
    code: String,
    isCompleted: Boolean,
    isSeniorMode: Boolean,
    onClick: () -> Unit,
) {
    val palette = parcelPalette
    val context = LocalContext.current
    val showToast = LocalAppToast.current

    val bgColor = palette.badgeBg(palette.emerald)
    val codeColor = palette.emerald
    val completedBg = palette.chip
    val completedFg = palette.textSecondary.copy(alpha = 0.7f)

    val animatedBgColor by animateColorAsState(
        targetValue = if (isCompleted) completedBg else bgColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bgColor",
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isCompleted) completedFg else codeColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "textColor",
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .defaultMinSize(minHeight = if (isSeniorMode) 60.dp else 52.dp)
            .widthIn(min = if (isSeniorMode) 176.dp else 0.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        color = animatedBgColor,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "已取件",
                    tint = completedFg,
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = formatPickupCode(code),
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = animatedTextColor,
                style = getPickupCodeTextStyle(isSeniorMode),
            )
            Spacer(modifier = Modifier.width(10.dp))
            // 复制按钮（Web Copy，1.8s 反馈由全局 Toast 承接）
            Text(
                text = "📋",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        val clipboard = context.getSystemService(
                            Context.CLIPBOARD_SERVICE,
                        ) as? android.content.ClipboardManager
                        clipboard?.setPrimaryClip(
                            ClipData.newPlainText("取件码", formatPickupCode(code)),
                        )
                        showToast("取件码已复制")
                    }
                    .padding(4.dp),
            )
        }
    }
}
