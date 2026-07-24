package com.chenran.parcel.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.provider.Telephony
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.util.Properties

object PermissionUtil {
    fun hasSmsPermissions(context: Context): Boolean {
        if (isMIUI()) {
            return isMiuiSmsGranted(context)

        } else {
            // 标准Android动态申请
            return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 检测是否为MIUI系统
    fun isMIUI(): Boolean {
        if (!Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)) return false
        return try {
            loadBuildProps().getProperty("ro.miui.ui.version.name") != null
        } catch (e: Exception) {
            // 读取失败时按制造商保守判定
            true
        }
    }

    private fun loadBuildProps(): Properties =
        Properties().apply {
            File(Environment.getRootDirectory(), "build.prop").inputStream().use { load(it) }
        }

    fun showMiuiPermissionExplanationDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("权限申请说明")
        builder.setMessage("小米手机需要获取读取短信权限和通知类短信权限2个权限，以便能够自动读取件码。请点击确定前往设置页面开启这2个权限。")
        builder.setPositiveButton("确定") { dialog, which ->
            // 在这里调用跳转 MIUI 权限设置页的方法
            (context as? android.app.Activity)?.let { requestMiuiSmsPermission(it) }
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // 跳转MIUI权限设置页
    fun requestMiuiSmsPermission(activity: Activity) {
        try {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName(
                    "com.miui.securitycenter",
                    if (isMiuiV9OrHigher()) "com.miui.permcenter.permissions.PermissionsEditorActivity"
                    else "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
                )
                putExtra("extra_pkgname", activity.packageName)
            }
            activity.startActivityForResult(intent, 1)
        } catch (e: Exception) {
            // 备用方案：跳转系统设置
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivityForResult(intent, 1)
        }
    }

    private fun isMiuiV9OrHigher(): Boolean {
        return try {
            loadBuildProps().getProperty("ro.miui.ui.version.code", "0").toInt() >= 9
        } catch (e: Exception) {
            false
        }
    }

}
// 检测MIUI实际权限状态
private fun isMiuiSmsGranted(context: Context): Boolean {
    return try {
        // 尝试读取短信验证触发权限检查；cursor 为 null 说明查询未成功，保守按未授权处理
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY),
            null,
            null,
            null
        )?.use { true } ?: false
    } catch (e: SecurityException) {
        false
    }
}