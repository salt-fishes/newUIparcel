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
import com.chenran.parcel.util.getAllSaveData
import com.chenran.parcel.viewmodel.ParcelViewModel

class ParcelWidgetXL : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "miui.appwidget.action.APPWIDGET_UPDATE" ||
            action == "com.chenran.parcel.CUSTOM_SMS_ADDED") {
            ParcelWidget.handleProviderBroadcast(this, context, ParcelWidgetXL::class.java)
        } else {
            super.onReceive(context, intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        ParcelWidget.handleProviderBroadcast(this, context, ParcelWidgetXL::class.java)
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        if (context != null) ParcelWidget.refreshProviderAsync(context, ParcelWidgetXL::class.java)
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
            // 如果没有提供 appWidgetId，则更新所有实例
            if (appWidgetId == null) {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(
                    android.content.ComponentName(context, ParcelWidgetXL::class.java)
                )
                for (id in ids) {
                    updateSingleAppWidget(context, manager, id, viewModel)
                }
            } else {
                updateSingleAppWidget(context, appWidgetManager, appWidgetId, viewModel)
            }
        }

        private fun updateSingleAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            viewModel: ParcelViewModel?
        ) {
            ParcelWidget.updateSingleAppWidget(context, appWidgetManager, appWidgetId, viewModel, ParcelWidgetXL::class.java)
        }
        }
}