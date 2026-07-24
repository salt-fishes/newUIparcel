package com.chenran.parcel.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chenran.parcel.model.SmsData
import com.chenran.parcel.ui.components.ThemedChip
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 单条短信行。关怀模式采用上下两段结构：主信息单独占一行，时间与原文入口位于次行，
 * 避免大号取件码与右侧纵向元信息互相挤压。
 */
@Composable
fun SmsDataRow(
    smsData: SmsData,
    isSeniorMode: Boolean,
    showCodeTime: Boolean,
    onCodeClick: () -> Unit,
    onRawClick: () -> Unit,
) {
    ThemedChip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ParcelSpacing.xs),
        shape = RoundedCornerShape(16.dp),
    ) {
        if (isSeniorMode) {
            SeniorSmsContent(
                smsData = smsData,
                showCodeTime = showCodeTime,
                onCodeClick = onCodeClick,
                onRawClick = onRawClick,
            )
        } else {
            RegularSmsContent(
                smsData = smsData,
                showCodeTime = showCodeTime,
                onCodeClick = onCodeClick,
                onRawClick = onRawClick,
            )
        }
    }
}

@Composable
private fun SeniorSmsContent(
    smsData: SmsData,
    showCodeTime: Boolean,
    onCodeClick: () -> Unit,
    onRawClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(ParcelSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ParcelSpacing.sm),
        ) {
            if (smsData.rawBody.isNotEmpty()) {
                RawBodySummary(
                    rawBody = smsData.rawBody,
                    isSeniorMode = true,
                    modifier = Modifier.weight(1f),
                    isCompleted = smsData.isCompleted,
                    onToggleComplete = onCodeClick,
                )
            } else {
                PickupCodeChip(
                    code = smsData.code,
                    isCompleted = smsData.isCompleted,
                    isSeniorMode = true,
                    onClick = onCodeClick,
                )
            }
            LockerBadge(smsData.lockerNumber, true)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (showCodeTime) {
                SmsTimestamp(timestamp = smsData.sms.timestamp, isSeniorMode = true)
            } else {
                Text(
                    text = if (smsData.rawBody.isNotEmpty()) "部分识别结果" else "点击取件码切换状态",
                    style = MaterialTheme.typography.bodySmall,
                    color = parcelPalette.textSecondary,
                )
            }
            RawSmsButton(onRawClick = onRawClick, isSeniorMode = true)
        }
    }
}

@Composable
private fun RegularSmsContent(
    smsData: SmsData,
    showCodeTime: Boolean,
    onCodeClick: () -> Unit,
    onRawClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = if (smsData.rawBody.isNotEmpty()) Alignment.Top else Alignment.CenterVertically,
    ) {
        if (smsData.rawBody.isNotEmpty()) {
            RawBodySummary(
                rawBody = smsData.rawBody,
                isSeniorMode = false,
                modifier = Modifier.weight(1f),
                isCompleted = smsData.isCompleted,
                onToggleComplete = onCodeClick,
            )
        } else {
            PickupCodeChip(
                code = smsData.code,
                isCompleted = smsData.isCompleted,
                isSeniorMode = false,
                onClick = onCodeClick,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = ParcelSpacing.sm),
        ) {
            LockerBadge(smsData.lockerNumber, false)
            if (showCodeTime) {
                SmsTimestamp(timestamp = smsData.sms.timestamp, isSeniorMode = false)
            }
            RawSmsButton(onRawClick = onRawClick, isSeniorMode = false)
        }
    }
}

/**
 * 无取件码短信的摘要。onToggleComplete 非空时，点击切换已取/待取状态（全文可经“原文”查看）；
 * 为空时保持旧的点击展开/收起行为。
 */
@Composable
private fun RawBodySummary(
    rawBody: String,
    isSeniorMode: Boolean,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false,
    onToggleComplete: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                if (onToggleComplete != null) onToggleComplete() else expanded = !expanded
            }
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Text(
            text = if (expanded) rawBody else rawBody.take(80) + if (rawBody.length > 80) "…" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCompleted) parcelPalette.textSecondary else parcelPalette.textPrimary,
            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
        )
        if (onToggleComplete != null) {
            Text(
                text = if (isCompleted) "已取件 · 点击恢复待取" else "点击标记已取件",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) parcelPalette.textSecondary else parcelPalette.emerald,
                modifier = Modifier.padding(top = 2.dp),
            )
        } else if (!expanded && rawBody.length > 80) {
            Text(
                text = "点击展开",
                style = MaterialTheme.typography.bodySmall,
                color = parcelPalette.textSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun LockerBadge(lockerNumber: String, isSeniorMode: Boolean) {
    if (lockerNumber.isEmpty()) return
    val palette = parcelPalette
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = palette.badgeBg(palette.indigo),
        border = BorderStroke(1.dp, palette.badgeBorder(palette.indigo)),
    ) {
        Text(
            text = lockerNumber,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.indigo,
        )
    }
}

@Composable
private fun SmsTimestamp(timestamp: Long, isSeniorMode: Boolean) {
    val formatter = remember(isSeniorMode) {
        SimpleDateFormat(if (isSeniorMode) "M/d HH:mm" else "M/d HH:mm", Locale.getDefault())
    }
    Text(
        text = formatter.format(Date(timestamp)),
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        color = parcelPalette.textSecondary,
    )
}

@Composable
private fun RawSmsButton(onRawClick: () -> Unit, isSeniorMode: Boolean) {
    val palette = parcelPalette
    Surface(
        onClick = onRawClick,
        shape = RoundedCornerShape(8.dp),
        color = palette.chip,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = 10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                tint = palette.textSecondary,
            )
            Text(
                text = "原文",
                style = MaterialTheme.typography.labelLarge,
                color = palette.textSecondary,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}
