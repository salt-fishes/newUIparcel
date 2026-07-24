package com.chenran.parcel.ui.home.components

/**
 * v2.0 列表 Tab 模式（R10）：替代旧 showCompleted 开关。
 * 待取 Tab 只显示未取件，历史归档 Tab 只显示已取件（对齐 Web pending/archive）。
 */
enum class ParcelTabMode {
    PENDING,
    ARCHIVE,
}
