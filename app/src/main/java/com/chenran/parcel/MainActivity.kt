package com.chenran.parcel

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chenran.parcel.ui.AboutScreen
import com.chenran.parcel.ui.AddCustomSmsScreen
import com.chenran.parcel.ui.AddRuleScreen
import com.chenran.parcel.ui.AddressGroupScreen
import com.chenran.parcel.ui.FailSmsScreen
import com.chenran.parcel.ui.home.PendingTab
import com.chenran.parcel.ui.home.ArchiveTab
import com.chenran.parcel.ui.identity.IdentityScreen
import com.chenran.parcel.ui.main.MainScaffold
import com.chenran.parcel.ui.main.MainTabs
import com.chenran.parcel.ui.main.AppNavbar
import com.chenran.parcel.ui.settings.PreferencesScreen
import com.chenran.parcel.ui.settings.BackupScreen
import com.chenran.parcel.ui.settings.ThemeBgSheet
import com.chenran.parcel.ui.home.getShowCodeTime
import com.chenran.parcel.ui.home.saveShowCodeTime
import com.chenran.parcel.ui.home.getHorizontalLayout
import com.chenran.parcel.ui.home.saveHorizontalLayout
import com.chenran.parcel.util.getSortByLocker
import com.chenran.parcel.util.saveSortByLocker
import com.chenran.parcel.ui.RulesScreen
import com.chenran.parcel.ui.SuccessSmsScreen
import com.chenran.parcel.ui.UseNotificationScreen
import com.chenran.parcel.ui.theme.ParcelTheme
import com.chenran.parcel.util.ThemeManager
import com.chenran.parcel.ui.components.ThemeBackdrop
import com.chenran.parcel.util.PermissionUtil
import com.chenran.parcel.util.PermissionUtil.showMiuiPermissionExplanationDialog
import com.chenran.parcel.util.SeniorModeManager
import com.chenran.parcel.util.SmsParser
import com.chenran.parcel.util.SmsProcessor
import com.chenran.parcel.util.getAllSaveData
import com.chenran.parcel.util.getAiConfig
import com.chenran.parcel.util.getMainSwitch
import com.chenran.parcel.util.ai.AiParseScheduler
import com.chenran.parcel.viewmodel.ParcelViewModel
import com.chenran.parcel.widget.ParcelWidget
import com.chenran.parcel.widget.ParcelWidgetLarge
import com.chenran.parcel.widget.ParcelWidgetLargeMiui
import com.chenran.parcel.widget.ParcelWidgetMiui
import java.net.URLDecoder
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.chenran.parcel.widget.ParcelWidgetXL
import com.chenran.parcel.service.ParcelNotificationListenerService
import com.chenran.parcel.ui.LogScreen
import com.chenran.parcel.util.addLog
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.core.content.edit
import com.chenran.parcel.ui.SettingsScreen
import com.chenran.parcel.ui.ai.AiSettingsScreen
import com.chenran.parcel.ui.components.WallpaperBackdrop
import com.chenran.parcel.util.BackgroundManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.padding


class MainActivity : ComponentActivity() {
    private val hasPermissionState = mutableStateOf(false)
    internal val isSeniorModeState = mutableStateOf(false)
    private var smsContentObserver: ContentObserver? = null
    private lateinit var appDetailsLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
    private lateinit var viewModel: ParcelViewModel
    val context = this
    val smsParser = SmsParser()
    private var customSmsReceiver: BroadcastReceiver? = null
    private var smsRefreshJob: kotlinx.coroutines.Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasPermissionState.value = PermissionUtil.hasSmsPermissions(this)

        viewModel = ParcelViewModel(smsParser, applicationContext)
        isSeniorModeState.value = SeniorModeManager.getSeniorMode(applicationContext)


        // 注册权限请求 Launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                hasPermissionState.value = true
                readAndParseSms()
                startSmsDeletionMonitoring()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)) {
                    // 用户拒绝但可再次询问：无短信权限不能读取，交由界面引导重新授权
                    hasPermissionState.value = false
                } else {
                    guideToSettings()
                }
            }
        }

        // 注册 ActivityResultLauncher
        appDetailsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                init()

            }

        getAllSaveData(this, viewModel)
        init()

        setContent {
            App(
                context,
                viewModel,
                hasPermissionState.value,
                guideToSettings = { guideToSettings() },
                readAndParseSms = { readAndParseSms() },
                updateAllWidget = { updateAllWidget() },
                isSeniorMode = isSeniorModeState.value
            )
        }
    }


    private fun startSmsDeletionMonitoring() {
        smsContentObserver?.let { observer ->
            runCatching { contentResolver.unregisterContentObserver(observer) }
        }
        try {
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    readAndParseSms()
                }
            }
            contentResolver.registerContentObserver(
                "content://sms".toUri(),
                true,
                observer,
            )
            smsContentObserver = observer
        } catch (e: SecurityException) {
            Log.e("MainActivity", "Failed to register SMS observer: ${e.message}")
        }
    }

    fun updateAllWidget() {
        // 刷新 AppWidget（不传递 appWidgetId 以更新所有实例）
        ParcelWidget.updateAppWidget(
            context,
            AppWidgetManager.getInstance(context),
            null,
            viewModel
        )
        ParcelWidgetLarge.updateAppWidget(
            context,
            AppWidgetManager.getInstance(context),
            null,
            viewModel
        )
        ParcelWidgetXL.updateAppWidget(
            context,
            AppWidgetManager.getInstance(context),
            null,
            viewModel
        )
        ParcelWidgetMiui.updateAppWidget(
            context,
            AppWidgetManager.getInstance(context),
            null,
            viewModel
        )
        ParcelWidgetLargeMiui.updateAppWidget(
            context,
            AppWidgetManager.getInstance(context),
            null,
            viewModel
        )
    }

    fun readAndParseSms() {
        smsRefreshJob?.cancel()
        smsRefreshJob = lifecycleScope.launch {
            kotlinx.coroutines.delay(500L)
            try {
                val context = applicationContext
                val daysFilter = viewModel.timeFilterIndex.value
                val (smsList, customSmsList) = SmsProcessor.loadMessages(context, daysFilter)
                val allMessages = smsList + customSmsList
                AiParseScheduler.enqueueNewMessages(context, allMessages, getAiConfig(context))
                viewModel.getAllMessageWithCustom(smsList, customSmsList)

                updateAllWidget()
            } catch (e: SecurityException) {
                Log.e("MainActivity", "Failed to read SMS: ${e.message}")
            }
        }
    }


    private fun init() {
        hasPermissionState.value = PermissionUtil.hasSmsPermissions(this)
        if (PermissionUtil.isMIUI()) {
            //小米手机 MIUI widget启用
            val component = ComponentName(context, ParcelWidgetMiui::class.java)
            context.packageManager.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            val componentLarge = ComponentName(context, ParcelWidgetLargeMiui::class.java)
            context.packageManager.setComponentEnabledSetting(
                componentLarge,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        // 检查并请求短信权限
        if (!PermissionUtil.hasSmsPermissions(this)) {
            if (PermissionUtil.isMIUI()) {
                //小米手机 显示引导弹窗后调用权限请求
                showMiuiPermissionExplanationDialog(context)
            }
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                )
            )
        } else {
            // 权限已授予，读取短信
            readAndParseSms()
            startSmsDeletionMonitoring()
        }
        // 注册接收“自定义短信已添加”的广播，统一触发 UI 刷新
        registerCustomSmsAddedReceiver()

        // 应用启动后尝试重新绑定通知监听服务，避免重启后不工作
        rebindNotificationListenerIfNeeded()

        scheduleNotificationListenerHealthCheck()
    }

    private fun registerCustomSmsAddedReceiver() {
        customSmsReceiver?.let { receiver ->
            runCatching { unregisterReceiver(receiver) }
        }
        val action = "com.chenran.parcel.CUSTOM_SMS_ADDED"
        val parseAction = com.chenran.parcel.worker.AiParseWorker.ACTION_PARSE_UPDATED
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == action || intent.action == parseAction) {
                    readAndParseSms()
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(action)
            addAction(parseAction)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(receiver, filter)
        }
        customSmsReceiver = receiver
    }

    private fun rebindNotificationListenerIfNeeded() {
        try {
            val hasAccess = hasNotificationAccess(this)
            val mainEnabled = getMainSwitch(this)
            if (hasAccess && mainEnabled) {
                NotificationListenerService.requestRebind(
                    ComponentName(this, ParcelNotificationListenerService::class.java)
                )
                Log.d("MainActivity", "Requested rebind on app start")
            } else {
                Log.d("MainActivity", "Skip rebind: hasAccess=$hasAccess mainEnabled=$mainEnabled")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Request rebind failed: ${e.message}")
            addLog(this, "重新绑定通知监听失败: ${e.message}")
        }
    }

    private fun scheduleNotificationListenerHealthCheck() {
        try {
            val healthWork = PeriodicWorkRequestBuilder<
                com.chenran.parcel.worker.NotificationListenerHealthWorker
            >(15, java.util.concurrent.TimeUnit.MINUTES).build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                com.chenran.parcel.worker.NotificationListenerHealthWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                healthWork
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to schedule health check: ${e.message}")
        }
    }

    private fun hasNotificationAccess(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (flat.isNullOrBlank()) return false
        val full = ComponentName(context, ParcelNotificationListenerService::class.java).flattenToString()
        val short = "${context.packageName}/.service.ParcelNotificationListenerService"
        return flat.split(":").any { it == full || it == short }
    }

    private fun guideToSettings() {
        val uri = Uri.fromParts("package", "com.chenran.parcel", null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = uri
        }
        appDetailsLauncher.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消短信删除监控
        smsContentObserver?.let { observer ->
            try {
                contentResolver.unregisterContentObserver(observer)
            } catch (_: Exception) { }
        }
        // 取消自定义短信广播接收
        customSmsReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) { }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
        context: Context,
        viewModel: ParcelViewModel,
        hasPermission: Boolean,
        guideToSettings: () -> Unit,
        readAndParseSms: () -> Unit,
        updateAllWidget: () -> Unit,
        isSeniorMode: Boolean
    ) {
    var themeMode by remember { mutableStateOf(ThemeManager.getThemeMode(context)) }
    var wallpaperRevision by remember { mutableStateOf(0) }
    val wallpaperState = remember(wallpaperRevision) {
        Triple(
            BackgroundManager.isWallpaperEnabled(context),
            BackgroundManager.getHomeWallpaperUri(context),
            BackgroundManager.getWallpaperSettings(context),
        )
    }
    val wallpaperActive = wallpaperState.first && wallpaperState.second.isNotBlank()
        ParcelTheme(themeMode = themeMode, isSeniorMode = isSeniorMode) {
        val navController = rememberNavController()
        // v2.0 待取总数（底部 Tab 角标，R10）
        val parcelsData by viewModel.parcelsData.collectAsState()
        val pendingCount = parcelsData.sumOf { it.num }
        // v2.0 视图偏好上提（R11）：AppNavbar 下拉与列表共享
        var sortByLocker by remember { mutableStateOf(getSortByLocker(context)) }
        var isHorizontalLayout by remember { mutableStateOf(getHorizontalLayout(context)) }
        var showCodeTime by remember { mutableStateOf(getShowCodeTime(context)) }
        // v2.0 主题壁纸弹层（R12）：AppNavbar 与设置页共享入口
        var showThemeBgSheet by remember { mutableStateOf(false) }

        // 首次启动弹窗：询问是否开启老人模式
        var showSeniorPrompt by remember {
            val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
            val shown = prefs.getBoolean("senior_prompt_shown", false)
            mutableStateOf(!shown)
        }

        if (showSeniorPrompt) {
            AlertDialog(
                onDismissRequest = {
                    showSeniorPrompt = false
                    val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
                    prefs.edit { putBoolean("senior_prompt_shown", true) }
                    SeniorModeManager.saveSeniorMode(context, false)
                    (context as? MainActivity)?.isSeniorModeState?.value = false
                    updateAllWidget()
                },
                title = {
                    Text("开启老人模式？", style = MaterialTheme.typography.headlineSmall)
                },
                text = {
                    Text("字更大，老人优先", style = MaterialTheme.typography.bodyLarge)
                },
                confirmButton = {
                    TextButton(onClick = {
                        showSeniorPrompt = false
                        val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
                        prefs.edit { putBoolean("senior_prompt_shown", true) }
                        SeniorModeManager.saveSeniorMode(context, true)
                        (context as? MainActivity)?.isSeniorModeState?.value = true
                        updateAllWidget()
                    }) {
                        Text("开启", style = MaterialTheme.typography.labelLarge)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSeniorPrompt = false
                        val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
                        prefs.edit { putBoolean("senior_prompt_shown", true) }
                        SeniorModeManager.saveSeniorMode(context, false)
                        (context as? MainActivity)?.isSeniorModeState?.value = false
                        updateAllWidget()
                    }) {
                        Text("不用", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // v2.0 主题渐变背景层（R9）：壁纸开启时由壁纸层覆盖
            ThemeBackdrop()
            if (wallpaperActive) {
                WallpaperBackdrop(
                    wallpaperUri = wallpaperState.second,
                    settings = wallpaperState.third,
                )
            }
            // v2.0 全局骨架（R10）：底部 4 Tab + 全局 FAB + 全局 Toast
            MainScaffold(
                navController = navController,
                pendingCount = pendingCount,
                hasPermission = hasPermission,
                isSeniorMode = isSeniorMode,
                onFabClick = { navController.navigate("add_custom_sms") },
                topBar = {
                    AppNavbar(
                        isSeniorMode = isSeniorMode,
                        onToggleSenior = {
                            val new = !isSeniorMode
                            SeniorModeManager.saveSeniorMode(context, new)
                            (context as? MainActivity)?.isSeniorModeState?.value = new
                            updateAllWidget()
                        },
                        sortByLocker = sortByLocker,
                        isHorizontalLayout = isHorizontalLayout,
                        showCodeTime = showCodeTime,
                        onToggleSort = {
                            val new = !sortByLocker
                            saveSortByLocker(context, new)
                            sortByLocker = new
                            readAndParseSms()
                        },
                        onToggleLayout = {
                            val new = !isHorizontalLayout
                            saveHorizontalLayout(context, new)
                            isHorizontalLayout = new
                        },
                        onToggleTime = {
                            val new = !showCodeTime
                            saveShowCodeTime(context, new)
                            showCodeTime = new
                        },
                        onOpenTheme = { showThemeBgSheet = true },
                    )
                },
            ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MainTabs.ROUTE_PENDING,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(MainTabs.ROUTE_PENDING) {
                    PendingTab(
                        context = context,
                        viewModel = viewModel,
                        navController = navController,
                        hasPermission = hasPermission,
                        onCallBack = { guideToSettings() },
                        updateAllWidget = updateAllWidget,
                        isSeniorMode = isSeniorMode,
                        sortByLocker = sortByLocker,
                        isHorizontalLayout = isHorizontalLayout,
                        showCodeTime = showCodeTime,
                    )
                }
                composable(MainTabs.ROUTE_ARCHIVE) {
                    ArchiveTab(
                        context = context,
                        viewModel = viewModel,
                        navController = navController,
                        hasPermission = hasPermission,
                        onCallBack = { guideToSettings() },
                        updateAllWidget = updateAllWidget,
                        isSeniorMode = isSeniorMode,
                        sortByLocker = sortByLocker,
                        isHorizontalLayout = isHorizontalLayout,
                        showCodeTime = showCodeTime,
                    )
                }
                composable(MainTabs.ROUTE_IDENTITY) {
                    IdentityScreen(context = context, isSeniorMode = isSeniorMode)
                }
                composable(MainTabs.ROUTE_SETTINGS) {
                    SettingsScreen(
                        navController = navController,
                        viewModel = viewModel,
                        isSeniorMode = isSeniorMode,
                        onOpenThemeBg = { showThemeBgSheet = true },
                    )
                }
                composable("ai_settings") {
                    AiSettingsScreen(navController = navController)
                }
                composable("preferences") {
                    PreferencesScreen(
                        navController = navController,
                        sortByLocker = sortByLocker,
                        isHorizontalLayout = isHorizontalLayout,
                        showCodeTime = showCodeTime,
                        isSeniorMode = isSeniorMode,
                        onToggleSort = { enabled ->
                            saveSortByLocker(context, enabled)
                            sortByLocker = enabled
                            readAndParseSms()
                        },
                        onToggleLayout = { enabled ->
                            saveHorizontalLayout(context, enabled)
                            isHorizontalLayout = enabled
                        },
                        onToggleTime = { enabled ->
                            saveShowCodeTime(context, enabled)
                            showCodeTime = enabled
                        },
                        onToggleSenior = { enabled ->
                            SeniorModeManager.saveSeniorMode(context, enabled)
                            (context as? MainActivity)?.isSeniorModeState?.value = enabled
                            updateAllWidget()
                        },
                    )
                }
                composable("backup") {
                    BackupScreen(navController = navController, viewModel = viewModel)
                }
                composable(
                    route = "add_custom_sms?address={address}",
                    arguments = listOf(
                        navArgument("address") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val address = backStackEntry.arguments?.getString("address") ?: ""
                    AddCustomSmsScreen(
                        context,
                        viewModel,
                        navController,
                        URLDecoder.decode(address, "UTF-8"),
                        onCallback = { readAndParseSms() }
                    )
                }
                composable(
                    route = "add_rule?message={message}",
                    arguments = listOf(
                        navArgument("message") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val message = backStackEntry.arguments?.getString("message") ?: ""
                    AddRuleScreen(
                        context,
                        viewModel,
                        navController,
                        URLDecoder.decode(message, "UTF-8"),
                        onCallback = { readAndParseSms() })
                }
                composable("address_group") {
                    AddressGroupScreen(context, navController, onCallback = { readAndParseSms() })
                }
                composable("rules") {
                    RulesScreen(
                        context,
                        viewModel,
                        navController,
                        onCallback = { readAndParseSms() })
                }
                composable("fail_sms") {
                    FailSmsScreen(viewModel, navController, isSeniorMode, readAndParseSms)
                }
                composable("success_sms") {
                    SuccessSmsScreen(viewModel, navController, isSeniorMode, readAndParseSms)
                }
                composable("about") {
                    AboutScreen(navController)
                }
                composable("use_notification") {
                    UseNotificationScreen(navController)
                }
                composable("logs") {
                    LogScreen(navController)
                }
            }
            }

            // v2.0 主题壁纸弹层（R12）
            if (showThemeBgSheet) {
                ThemeBgSheet(
                    themeMode = themeMode,
                    onThemeModeChange = { mode ->
                        ThemeManager.setThemeMode(context, mode)
                        themeMode = mode
                    },
                    isSeniorMode = isSeniorMode,
                    onToggleSenior = { enabled ->
                        SeniorModeManager.saveSeniorMode(context, enabled)
                        (context as? MainActivity)?.isSeniorModeState?.value = enabled
                        updateAllWidget()
                    },
                    onWallpaperChanged = { wallpaperRevision += 1 },
                    onDismiss = { showThemeBgSheet = false },
                )
            }
        }
    }
}
