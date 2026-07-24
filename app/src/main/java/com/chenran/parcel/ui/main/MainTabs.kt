package com.chenran.parcel.ui.main

/**
 * v2.0 底部 4 Tab 路由定义（R10，对齐 Web activeTab）。
 */
object MainTabs {
    const val ROUTE_PENDING = "tab_pending"
    const val ROUTE_ARCHIVE = "tab_archive"
    const val ROUTE_IDENTITY = "tab_identity"
    const val ROUTE_SETTINGS = "tab_settings"

    val roots = listOf(
        ROUTE_PENDING,
        ROUTE_ARCHIVE,
        ROUTE_IDENTITY,
        ROUTE_SETTINGS,
    )

    /** 底栏与全局 FAB 仅在 4 个 Tab 根路由显示。 */
    fun isTabRoot(route: String?): Boolean = route in roots
}
