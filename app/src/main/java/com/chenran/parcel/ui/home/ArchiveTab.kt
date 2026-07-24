package com.chenran.parcel.ui.home

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.chenran.parcel.ui.home.components.ParcelTabMode
import com.chenran.parcel.viewmodel.ParcelViewModel

/** v2.0 历史归档 Tab（R10）：只显示已取件。 */
@Composable
fun ArchiveTab(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    hasPermission: Boolean,
    onCallBack: () -> Unit,
    updateAllWidget: () -> Unit,
    isSeniorMode: Boolean,
    sortByLocker: Boolean,
    isHorizontalLayout: Boolean,
    showCodeTime: Boolean,
) {
    ParcelTabContent(
        context = context,
        viewModel = viewModel,
        navController = navController,
        hasPermission = hasPermission,
        onCallBack = onCallBack,
        updateAllWidget = updateAllWidget,
        isSeniorMode = isSeniorMode,
        tabMode = ParcelTabMode.ARCHIVE,
        sortByLocker = sortByLocker,
        isHorizontalLayout = isHorizontalLayout,
        showCodeTime = showCodeTime,
    )
}
