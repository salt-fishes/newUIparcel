package com.chenran.parcel.service

import android.graphics.drawable.Icon
import android.content.Context
import android.content.ComponentName
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.chenran.parcel.util.getMainSwitch
import com.chenran.parcel.util.setMainSwitch
import android.content.Intent
import android.service.notification.NotificationListenerService
import com.chenran.parcel.R

class NotificationAccessTileService : TileService() {
    override fun onStartListening() {
        val tile = qsTile ?: return
        val mainEnabled = getMainSwitch(this)
        val hasAccess = isNotificationAccessGranted(this)
        tile.state = if (mainEnabled && hasAccess) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.app_name)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_notification)
        tile.updateTile()
        val t = System.currentTimeMillis()
        if (t - lastFixTime > 3000L) {
            lastFixTime = t
            fixRestartNotificationListener()
        }
    }

    override fun onClick() {
        val hasAccess = isNotificationAccessGranted(this)
        val mainEnabled = getMainSwitch(this)
        if (!hasAccess) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityAndCollapse(intent)
        } else {
            setMainSwitch(this, !mainEnabled)
            if (!mainEnabled) {
                try {
                    NotificationListenerService.requestRebind(ComponentName(this, ParcelNotificationListenerService::class.java))
                } catch (_: Exception) { }
                fixRestartNotificationListener()
            }
        }
        onStartListening()
    }

    private fun isNotificationAccessGranted(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (flat.isNullOrBlank()) return false
        val full = ComponentName(context, ParcelNotificationListenerService::class.java).flattenToString()
        val short = "${context.packageName}/.service.ParcelNotificationListenerService"
        return flat.split(":").any { it == full || it == short }
    }

    private fun fixRestartNotificationListener() {
        // 移除写 Settings.Secure 的死代码（普通应用无 WRITE_SECURE_SETTINGS 权限，必然静默失败）；
        // 优先 requestRebind，失败时禁用/启用组件触发系统重绑
        val cn = ComponentName(this, ParcelNotificationListenerService::class.java)
        try {
            NotificationListenerService.requestRebind(cn)
        } catch (_: Exception) {
            runCatching {
                val pm = packageManager
                pm.setComponentEnabledSetting(cn, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED, android.content.pm.PackageManager.DONT_KILL_APP)
                pm.setComponentEnabledSetting(cn, android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED, android.content.pm.PackageManager.DONT_KILL_APP)
            }
        }
    }

    companion object {
        private var lastFixTime = 0L
    }
}
