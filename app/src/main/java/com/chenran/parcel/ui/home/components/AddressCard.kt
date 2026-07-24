package com.chenran.parcel.ui.home.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chenran.parcel.MainActivity
import com.chenran.parcel.model.ParcelData
import com.chenran.parcel.ui.components.IconBox
import com.chenran.parcel.ui.components.RawSmsDialog
import com.chenran.parcel.ui.components.TagDialog
import com.chenran.parcel.ui.components.ThemedCard
import com.chenran.parcel.ui.components.ThemedChip
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette
import com.chenran.parcel.util.addCompletedIds
import com.chenran.parcel.util.getAddressMappings
import com.chenran.parcel.util.removeAddressMapping
import com.chenran.parcel.util.removeCompletedId
import com.chenran.parcel.util.saveAddressMapping
import com.chenran.parcel.viewmodel.ParcelViewModel

/**
 * 地址分组卡片（v2.0 R11，对齐 Web ParcelCard）：
 * 紫图标盒 + 地址标题 + 标签胶囊 + "N 件待取"(emerald) + 一键取件 + 折叠箭头；
 * 体部为取件码行列表（待取 Tab 仅未取 / 历史 Tab 仅已取）。
 */
@SuppressLint("MutableCollectionMutableState")
@Composable
fun AddressCard(
    context: Context,
    viewModel: ParcelViewModel,
    updateAllWidget: () -> Unit,
    tabMode: ParcelTabMode,
    showCodeTime: Boolean,
    parcelData: ParcelData,
    expandedStates: MutableState<MutableMap<String, Boolean>>,
    isExpanded: Boolean,
    isSeniorMode: Boolean,
) {
    val palette = parcelPalette
    val isAllCompleted = parcelData.smsDataList.find { !it.isCompleted } == null
    var showTagDialog by remember { mutableStateOf(false) }
    var showSmsDetail by remember { mutableStateOf<String?>(null) }
    // 地址标签映射异步加载，随 parcelData 变化重载，避免每次重组同步读盘
    val addressMappings by produceState(initialValue = emptyMap<String, String>(), parcelData) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            getAddressMappings(context)
        }
    }
    val isGroupedByTag = parcelData.groupKey.startsWith("tag:")
    val currentTag = if (isGroupedByTag) {
        parcelData.address
    } else {
        addressMappings[parcelData.smsDataList.firstOrNull()?.address ?: ""] ?: ""
    }
    val displayTag = if (isGroupedByTag) parcelData.address else currentTag

    fun toggleExpand() {
        expandedStates.value = expandedStates.value.toMutableMap().apply {
            put(parcelData.groupKey, !isExpanded)
        }
    }

    ThemedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ParcelSpacing.xs),
        cornerRadius = 24.dp,
    ) {
        // 卡片头：地址信息与操作区上下两行（普通/关怀一致），组名独占一行避免被操作按钮挤压截断。
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { toggleExpand() },
            verticalArrangement = Arrangement.spacedBy(if (isSeniorMode) ParcelSpacing.sm else ParcelSpacing.xs),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBox(
                    color = palette.purple,
                    size = if (isSeniorMode) 48.dp else 40.dp,
                    cornerRadius = 16.dp,
                ) {
                    Text(text = "📦", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.width(ParcelSpacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = parcelData.address,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = palette.textPrimary,
                    )
                    Text(
                        text = if (!isAllCompleted) "${parcelData.num} 件待取" else "全部已取完",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (!isAllCompleted) palette.emerald else palette.textSecondary,
                    )
                }
                IconButton(onClick = { toggleExpand() }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "收起列表" else "展开列表",
                        tint = palette.textSecondary,
                    )
                }
            }
            AddressActions(
                displayTag = displayTag,
                hasPending = parcelData.num > 0,
                onTagClick = { showTagDialog = true },
                onCompleteAll = {
                    val smsList = parcelData.smsDataList.filterNot { it.isCompleted }.map { it.sms }
                    addCompletedIds(context, viewModel, smsList)
                    updateAllWidget()
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // 体部：取件码行列表
        AnimatedVisibility(
            visible = isExpanded,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(top = ParcelSpacing.sm)) {
                parcelData.smsDataList.forEach { smsData ->
                    val visibleRow = when (tabMode) {
                        ParcelTabMode.PENDING -> !smsData.isCompleted
                        ParcelTabMode.ARCHIVE -> smsData.isCompleted
                    }
                    if (visibleRow) {
                        SmsDataRow(
                            smsData = smsData,
                            isSeniorMode = isSeniorMode,
                            showCodeTime = showCodeTime,
                            onCodeClick = {
                                if (smsData.isCompleted) {
                                    removeCompletedId(context, viewModel, smsData.sms)
                                } else {
                                    addCompletedIds(context, viewModel, listOf(smsData.sms))
                                }
                                updateAllWidget()
                            },
                            onRawClick = { showSmsDetail = smsData.sms.body },
                        )
                    }
                }
            }
        }
    }

    if (showTagDialog) {
        val originalAddresses = parcelData.smsDataList.map { it.address }.distinct()
        TagDialog(
            currentAddress = parcelData.address,
            currentTag = displayTag,
            existingMappings = addressMappings,
            onDismiss = { showTagDialog = false },
            onConfirm = { tag ->
                originalAddresses.forEach { addr ->
                    saveAddressMapping(context, addr, tag)
                }
                showTagDialog = false
                (context as? MainActivity)?.readAndParseSms()
            },
            onRemove = {
                originalAddresses.forEach { addr ->
                    removeAddressMapping(context, addr)
                }
                showTagDialog = false
                (context as? MainActivity)?.readAndParseSms()
            },
        )
    }

    if (showSmsDetail != null) {
        RawSmsDialog(
            body = showSmsDetail ?: "",
            onDismiss = { showSmsDetail = null },
        )
    }
}

@Composable
private fun AddressActions(
    displayTag: String,
    hasPending: Boolean,
    onTagClick: () -> Unit,
    onCompleteAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = parcelPalette
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ThemedChip(
            color = if (displayTag.isNotBlank()) palette.badgeBg(palette.purple) else palette.chip,
            onClick = onTagClick,
        ) {
            Text(
                text = if (displayTag.isNotBlank()) displayTag else "设标签",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (displayTag.isNotBlank()) palette.purple else palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(max = 112.dp)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
            )
        }
        if (hasPending) {
            ThemedChip(color = palette.badgeBg(palette.emerald), onClick = onCompleteAll) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = palette.emerald,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                    Text(
                        text = "一键取件",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.emerald,
                    )
                }
            }
        }
    }
}
