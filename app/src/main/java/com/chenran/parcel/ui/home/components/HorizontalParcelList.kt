package com.chenran.parcel.ui.home.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chenran.parcel.model.ParcelData
import com.chenran.parcel.ui.components.ThemedChip
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette
import com.chenran.parcel.viewmodel.ParcelViewModel
import kotlinx.coroutines.launch

/**
 * 横向布局（v2.0 R11，对齐 Web）：顶部横向滚动胶囊 Tab 条（每组"地址 (N)"，
 * 选中实心 emerald），下方 HorizontalPager 只渲染选中组的单个卡片。
 */
@SuppressLint("MutableCollectionMutableState")
@Composable
fun HorizontalParcelList(
    context: Context,
    viewModel: ParcelViewModel,
    updateAllWidget: () -> Unit,
    tabMode: ParcelTabMode,
    showCodeTime: Boolean,
    parcelsData: List<ParcelData>,
    expandedStates: MutableState<MutableMap<String, Boolean>>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    isSeniorMode: Boolean,
) {
    val palette = parcelPalette
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { parcelsData.size },
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        onTabSelected(pagerState.currentPage)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = ParcelSpacing.md, vertical = ParcelSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(ParcelSpacing.sm),
        ) {
            itemsIndexed(parcelsData) { index, data ->
                val isSelected = pagerState.currentPage == index
                val count = when (tabMode) {
                    ParcelTabMode.PENDING -> data.smsDataList.count { !it.isCompleted }
                    ParcelTabMode.ARCHIVE -> data.smsDataList.count { it.isCompleted }
                }
                ThemedChip(
                    color = if (isSelected) palette.emerald else Color.Unspecified,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                ) {
                    Text(
                        text = "${data.address} ($count)",
                        color = if (isSelected) palette.onFab else palette.textPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        ) { page ->
            val parcel = parcelsData[page]
            val isExpanded = expandedStates.value[parcel.groupKey] ?: true
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isSeniorMode) 12.dp else ParcelSpacing.md),
                contentPadding = PaddingValues(bottom = if (isSeniorMode) 120.dp else 88.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    AddressCard(
                        context = context,
                        viewModel = viewModel,
                        updateAllWidget = updateAllWidget,
                        tabMode = tabMode,
                        showCodeTime = showCodeTime,
                        parcelData = parcel,
                        expandedStates = expandedStates,
                        isExpanded = isExpanded,
                        isSeniorMode = isSeniorMode,
                    )
                }
            }
        }
    }
}
