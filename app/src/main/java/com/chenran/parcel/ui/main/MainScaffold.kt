package com.chenran.parcel.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette
import kotlinx.coroutines.delay

/** 全局 Toast（对齐 Web 绿色胶囊 Toast，2.4s 自动消失）。 */
val LocalAppToast = staticCompositionLocalOf<(String) -> Unit> { {} }

private data class MainTabItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

/**
 * v2.0 全局骨架（R10）：底部 4 Tab（待取角标）+ 全局渐变 FAB + 全局 Toast 层。
 * 底栏与 FAB 仅在 4 个 Tab 根路由显示。
 */
@Composable
fun MainScaffold(
    navController: NavController,
    pendingCount: Int,
    hasPermission: Boolean,
    isSeniorMode: Boolean,
    onFabClick: () -> Unit,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val palette = parcelPalette
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val onTabRoot = MainTabs.isTabRoot(currentRoute)

    var toastMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2400L)
            toastMessage = null
        }
    }

    val tabs = if (isSeniorMode) {
        listOf(
            MainTabItem(MainTabs.ROUTE_PENDING, "待取", Icons.Outlined.Home),
            MainTabItem(MainTabs.ROUTE_ARCHIVE, "归档", Icons.Outlined.List),
            MainTabItem(MainTabs.ROUTE_IDENTITY, "身份码", Icons.Outlined.AccountBox),
            MainTabItem(MainTabs.ROUTE_SETTINGS, "设置", Icons.Outlined.Settings),
        )
    } else {
        listOf(
            MainTabItem(MainTabs.ROUTE_PENDING, "待取包裹", Icons.Outlined.Home),
            MainTabItem(MainTabs.ROUTE_ARCHIVE, "历史归档", Icons.Outlined.List),
            MainTabItem(MainTabs.ROUTE_IDENTITY, "身份码", Icons.Outlined.AccountBox),
            MainTabItem(MainTabs.ROUTE_SETTINGS, "设置与功能", Icons.Outlined.Settings),
        )
    }

    CompositionLocalProvider(LocalAppToast provides { msg: String -> toastMessage = msg }) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    // v2.0 全局 AppNavbar：仅在 Tab 根路由显示
                    if (onTabRoot) {
                        topBar()
                    }
                },
                bottomBar = {
                    if (onTabRoot) {
                        NavigationBar(
                            modifier = Modifier.height(if (isSeniorMode) 88.dp else 80.dp),
                            containerColor = palette.panel,
                            tonalElevation = 0.dp,
                        ) {
                            tabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (!selected) {
                                            navController.navigate(tab.route) {
                                                popUpTo(MainTabs.ROUTE_PENDING) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (tab.route == MainTabs.ROUTE_PENDING && pendingCount > 0) {
                                                    Badge(
                                                        containerColor = palette.emerald,
                                                        contentColor = palette.onFab,
                                                    ) {
                                                        Text(if (pendingCount > 99) "99+" else pendingCount.toString())
                                                    }
                                                }
                                            },
                                        ) {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = tab.label,
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = palette.emerald,
                                        selectedTextColor = palette.emerald,
                                        unselectedIconColor = palette.textSecondary,
                                        unselectedTextColor = palette.textSecondary,
                                        indicatorColor = palette.badgeBg(palette.emerald),
                                    ),
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if (onTabRoot && hasPermission) {
                        // emerald→teal 渐变 FAB（对齐 Web）
                        Surface(
                            onClick = onFabClick,
                            shape = RoundedCornerShape(50),
                            color = Color.Transparent,
                            shadowElevation = 6.dp,
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(palette.fabStart, palette.fabEnd),
                                        ),
                                    )
                                    .defaultMinSize(minHeight = if (isSeniorMode) 56.dp else 48.dp)
                                    .padding(
                                        horizontal = if (isSeniorMode) 18.dp else 20.dp,
                                        vertical = if (isSeniorMode) 12.dp else 14.dp,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(ParcelSpacing.xs),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "录入短信",
                                    tint = palette.onFab,
                                )
                                Text(
                                    text = "录入短信",
                                    color = palette.onFab,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }
                },
                content = content,
            )

            // 全局 Toast 层：底部居中绿色胶囊（对齐 Web，2.4s 自动消失）
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isSeniorMode) 108.dp else 96.dp),
            ) {
                Row(
                    modifier = Modifier
                        .background(palette.emerald, RoundedCornerShape(50))
                        .padding(horizontal = ParcelSpacing.md, vertical = ParcelSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ParcelSpacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = palette.onFab,
                    )
                    Text(
                        text = toastMessage.orEmpty(),
                        color = palette.onFab,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}
