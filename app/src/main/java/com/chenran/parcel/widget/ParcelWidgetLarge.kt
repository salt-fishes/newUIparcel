package com.chenran.parcel.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.chenran.parcel.MainActivity
import com.chenran.parcel.R
import com.chenran.parcel.viewmodel.ParcelViewModel
import com.chenran.parcel.widget.ParcelWidget.Companion
import com.chenran.parcel.util.getCustomList
import com.chenran.parcel.util.getCustomSmsList
import com.chenran.parcel.util.getAllSaveData
import com.chenran.parcel.util.SmsParser
import com.chenran.parcel.util.isSameDay

class ParcelWidgetLarge : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "miui.appwidget.action.APPWIDGET_UPDATE" ||
            action == "com.chenran.parcel.CUSTOM_SMS_ADDED") {
            ParcelWidget.handleProviderBroadcast(this, context, ParcelWidgetLarge::class.java)
        } else {
            super.onReceive(context, intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        ParcelWidget.handleProviderBroadcast(this, context, ParcelWidgetLarge::class.java)
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        // 修复复制粘贴错误：刷新本 provider 而非 ParcelWidget
        if (context != null) ParcelWidget.refreshProviderAsync(context, ParcelWidgetLarge::class.java)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        // 当最后一个小部件被移除时调用
    }

        companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int?,
            viewModel: ParcelViewModel?
        ) {
            ParcelWidget.updateAllByProvider(context, ParcelWidgetLarge::class.java, viewModel)
        }

        private fun updateSingleAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            viewModel: ParcelViewModel?
        ) {
            ParcelWidget.updateSingleAppWidget(context, appWidgetManager, appWidgetId, viewModel, ParcelWidgetLarge::class.java)
        }
        }
}
