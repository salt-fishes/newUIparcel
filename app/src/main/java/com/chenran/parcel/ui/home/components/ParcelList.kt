package com.chenran.parcel.ui.home.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chenran.parcel.ui.components.EmptyState
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.viewmodel.ParcelViewModel

/**
 * 列表容器（v2.0 R10）：按 ParcelTabMode 过滤待取/历史；
 * 空状态引导添加；纵向 LazyColumn 或横向布局。添加入口由全局 FAB 承接。
 */
@SuppressLint("MutableCollectionMutableState")
@Composable
fun ParcelList(
    context: Context,
    viewModel: ParcelViewModel,
    updateAllWidget: () -> Unit,
    tabMode: ParcelTabMode,
    showCodeTime: Boolean,
    isHorizontalLayout: Boolean = false,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    isSeniorMode: Boolean,
    onAddClick: () -> Unit,
) {
    val parcelsData by viewModel.parcelsData.collectAsState()
    val filteredParcelsData = when (tabMode) {
        ParcelTabMode.PENDING -> parcelsData.filter { parcel ->
            parcel.smsDataList.any { !it.isCompleted }
        }
        ParcelTabMode.ARCHIVE -> parcelsData.filter { parcel ->
            parcel.smsDataList.any { it.isCompleted }
        }
    }
    val expandedStates = remember { mutableStateOf(mutableMapOf<String, Boolean>()) }
    var currentTabIndex by remember { mutableStateOf(selectedTabIndex) }
    val timeFilterIndex by viewModel.timeFilterIndex.collectAsState()

    LaunchedEffect(timeFilterIndex) {
        currentTabIndex = 0
    }

    if (isHorizontalLayout && filteredParcelsData.isNotEmpty()) {
        HorizontalParcelList(
            context = context,
            viewModel = viewModel,
            updateAllWidget = updateAllWidget,
            tabMode = tabMode,
            showCodeTime = showCodeTime,
            parcelsData = filteredParcelsData,
            expandedStates = expandedStates,
            selectedTabIndex = currentTabIndex,
            onTabSelected = {
                currentTabIndex = it
                onTabSelected(it)
            },
            isSeniorMode = isSeniorMode,
        )
        return
    }

    if (filteredParcelsData.isEmpty()) {
        EmptyState(
            title = if (tabMode == ParcelTabMode.PENDING) "暂无待取包裹" else "暂无已取件历史记录",
            description = if (tabMode == ParcelTabMode.PENDING) {
                "收到取件短信后会自动出现在这里，也可以手动添加"
            } else {
                "完成取件后可在这里回看记录"
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (tabMode == ParcelTabMode.PENDING) {
                Button(onClick = onAddClick) {
                    Text(
                        text = "添加自定义取件短信",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isSeniorMode) 12.dp else ParcelSpacing.md),
            contentPadding = PaddingValues(bottom = if (isSeniorMode) 120.dp else 88.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(filteredParcelsData, key = { it.groupKey }) { result ->
                val isExpanded = expandedStates.value[result.groupKey] ?: true
                AddressCard(
                    context = context,
                    viewModel = viewModel,
                    updateAllWidget = updateAllWidget,
                    tabMode = tabMode,
                    showCodeTime = showCodeTime,
                    parcelData = result,
                    expandedStates = expandedStates,
                    isExpanded = isExpanded,
                    isSeniorMode = isSeniorMode,
                )
            }
        }
    }
}
