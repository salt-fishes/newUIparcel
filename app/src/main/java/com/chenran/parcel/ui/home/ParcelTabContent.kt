package com.chenran.parcel.ui.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.chenran.parcel.MainActivity
import com.chenran.parcel.ui.components.EmptyState
import com.chenran.parcel.ui.components.ExportCardDialog
import com.chenran.parcel.ui.home.components.ListToolBar
import com.chenran.parcel.ui.home.components.ParcelList
import com.chenran.parcel.ui.home.components.ParcelTabMode
import com.chenran.parcel.ui.home.components.TimeFilterSheet
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette
import com.chenran.parcel.util.saveIndex
import com.chenran.parcel.viewmodel.ParcelViewModel
import kotlinx.coroutines.launch

/**
 * v2.0 待取/历史共享 Tab 内容（R11）：
 * ListToolBar（时间筛选 + 计数徽章）+ 列表 + 时间筛选弹层。
 * 视图偏好（排序/布局/时间显隐）已上提至 App 级 AppNavbar 下拉。
 */
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelTabContent(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    hasPermission: Boolean,
    onCallBack: () -> Unit,
    updateAllWidget: () -> Unit,
    isSeniorMode: Boolean,
    tabMode: ParcelTabMode,
    sortByLocker: Boolean,
    isHorizontalLayout: Boolean,
    showCodeTime: Boolean,
) {
    val palette = parcelPalette
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val timeFilterOptions = listOf(
        "全部", "今天", "近2天", "近3天", "近4天", "近5天",
        "近6天", "近7天", "近8天", "近9天", "近10天",
    )

    val selectedTimeFilterIndex by viewModel.timeFilterIndex.collectAsState()
    val failedData by viewModel.failedMessages.collectAsState()
    val successData by viewModel.successSmsData.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ListToolBar(
            timeFilterLabel = timeFilterOptions[selectedTimeFilterIndex.coerceIn(0, timeFilterOptions.lastIndex)],
            onTimeFilterClick = { showBottomSheet = true },
            successCount = successData.size,
            failCount = failedData.size,
            onSuccessClick = { navController.navigate("success_sms") },
            onFailClick = { navController.navigate("fail_sms") },
            onExportClick = { showExportDialog = true },
            isSeniorMode = isSeniorMode,
        )
        Spacer(modifier = Modifier.height(ParcelSpacing.xs))

        Box(modifier = Modifier.fillMaxSize()) {
            if (hasPermission) {
                ParcelList(
                    context = context,
                    viewModel = viewModel,
                    updateAllWidget = updateAllWidget,
                    tabMode = tabMode,
                    showCodeTime = showCodeTime,
                    isHorizontalLayout = isHorizontalLayout,
                    isSeniorMode = isSeniorMode,
                    onAddClick = { navController.navigate("add_custom_sms") },
                )
            } else {
                EmptyState(
                    title = "需要短信权限",
                    description = "授权后才能自动读取短信中的取件码",
                    emoji = "🔐",
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Button(
                        onClick = { onCallBack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.emerald,
                            contentColor = palette.onFab,
                        ),
                    ) {
                        Text("获取短信权限")
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ExportCardDialog(
            context = context,
            parcels = viewModel.parcelsData.collectAsState().value,
            onDismiss = { showExportDialog = false },
        )
    }

    if (showBottomSheet) {
        TimeFilterSheet(
            sheetState = sheetState,
            options = timeFilterOptions,
            selectedIndex = selectedTimeFilterIndex,
            isSeniorMode = isSeniorMode,
            onSelect = { index ->
                saveIndex(context, index)
                viewModel.setTimeFilterIndex(index)
                (context as? MainActivity)?.readAndParseSms()
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
            },
            onDismiss = { showBottomSheet = false },
        )
    }
}
