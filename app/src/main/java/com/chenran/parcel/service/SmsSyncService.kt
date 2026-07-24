package com.chenran.parcel.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.chenran.parcel.util.addLog
import com.chenran.parcel.util.getAiConfig
import com.chenran.parcel.util.ParcelMessageRepository
import com.chenran.parcel.util.ai.AiParseScheduler

class SmsSyncService : Service() {
    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= 26) {
            val mgr = getSystemService(NotificationManager::class.java)
            val id = "parcel_sync"
            if (mgr?.getNotificationChannel(id) == null) {
                val ch = NotificationChannel(id, "数据更新", NotificationManager.IMPORTANCE_MIN)
                ch.setShowBadge(false)
                ch.enableLights(false)
                ch.enableVibration(false)
                mgr?.createNotificationChannel(ch)
            }
            val notif = NotificationCompat.Builder(this, id)
                .setSmallIcon(com.chenran.parcel.R.drawable.ic_notification)
                .setContentTitle("数据更新")
                .setContentText("正在处理短信")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()
            // 显式 shortService 类型并降级保护：配额耗尽或权限异常时不崩溃，退化为后台处理
            runCatching {
                ServiceCompat.startForeground(
                    this,
                    1002,
                    notif,
                    if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE else 0,
                )
            }.onFailure { addLog(this, "前台服务启动失败，退化为后台处理: ${it.javaClass.simpleName}") }
        }
        super.onCreate()
    }

    override fun onTimeout(startId: Int) {
        // shortService 超时（约 3 分钟）时系统回调，必须停止前台状态并结束服务
        runCatching { ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE) }
        stopSelf(startId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            try {
                Thread.sleep(1200L)
                val (systemSms, customSms) = kotlinx.coroutines.runBlocking {
                    ParcelMessageRepository.load(applicationContext, 1)
                }
                AiParseScheduler.enqueueNewMessages(
                    applicationContext,
                    systemSms + customSms,
                    getAiConfig(applicationContext),
                )
                val i = Intent("com.chenran.parcel.CUSTOM_SMS_ADDED").setPackage(packageName)
                sendBroadcast(i)
                try {
                    val mgr = android.appwidget.AppWidgetManager.getInstance(this)
                    com.chenran.parcel.widget.ParcelWidget.updateAppWidget(this, mgr, null, null)
                    com.chenran.parcel.widget.ParcelWidgetLarge.updateAppWidget(this, mgr, null, null)
                    com.chenran.parcel.widget.ParcelWidgetXL.updateAppWidget(this, mgr, null, null)
                    com.chenran.parcel.widget.ParcelWidgetMiui.updateAppWidget(this, mgr, null, null)
                    com.chenran.parcel.widget.ParcelWidgetLargeMiui.updateAppWidget(this, mgr, null, null)
                } catch (_: Exception) {}
                addLog(this, "短信前台服务刷新完成并广播更新")
            } catch (e: Exception) { addLog(this, "短信服务出错: ${e.message}") }
            try { ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE) } catch (_: Exception) {}
            stopSelf(startId)
        }.start()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
