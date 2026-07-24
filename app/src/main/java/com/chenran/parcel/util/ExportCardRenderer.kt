package com.chenran.parcel.util

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.provider.MediaStore
import com.chenran.parcel.model.ParcelData
import com.chenran.parcel.model.SmsData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportCardPalette(val background: Int, val panel: Int, val accent: Int, val text: Int, val muted: Int, val codeBackground: Int) {
    DARK(0xFF020617.toInt(), 0xFF1E293B.toInt(), 0xFF34D399.toInt(), 0xFFF1F5F9.toInt(), 0xFF94A3B8.toInt(), 0xFF065F46.toInt()),
    EMERALD(0xFF022C22.toInt(), 0xFF064E3B.toInt(), 0xFF6EE7B7.toInt(), 0xFFECFDF5.toInt(), 0xFFA7F3D0.toInt(), 0xFF047857.toInt()),
    VIOLET(0xFF1E1B4B.toInt(), 0xFF3B0764.toInt(), 0xFFE9D5FF.toInt(), 0xFFFAF5FF.toInt(), 0xFFC4B5FD.toInt(), 0xFF6D28D9.toInt()),
    LIGHT(0xFFF1F5F9.toInt(), 0xFFFFFFFF.toInt(), 0xFF047857.toInt(), 0xFF0F172A.toInt(), 0xFF475569.toInt(), 0xFFD1FAE5.toInt()),
}

data class ExportCardOptions(
    val palette: ExportCardPalette = ExportCardPalette.DARK,
    val address: String? = null,
    val includeCompleted: Boolean = false,
)

object ExportCardRenderer {
    private const val WIDTH = 1280f
    private const val OUTER = 48f
    private const val HEADER = 170f
    private const val GROUP_HEADER = 110f
    private const val ROW_HEIGHT = 124f
    private const val MAX_HEIGHT = 8000
    private const val MAX_ITEMS = 50

    fun render(parcels: List<ParcelData>, options: ExportCardOptions): Bitmap {
        val groups = parcels.filter { options.address == null || it.address == options.address }.mapNotNull { parcel ->
            val items = parcel.smsDataList.filter { options.includeCompleted || !it.isCompleted }
            if (items.isEmpty()) null else parcel.copy(smsDataList = items.toMutableList())
        }
        val totalItems = groups.sumOf { it.smsDataList.size }
        // 限制导出条数，防止 Bitmap 高度无上限导致 OOM
        var remaining = MAX_ITEMS
        val limitedGroups = groups.mapNotNull { parcel ->
            if (remaining <= 0) return@mapNotNull null
            val take = minOf(parcel.smsDataList.size, remaining)
            remaining -= take
            if (take < parcel.smsDataList.size) parcel.copy(smsDataList = parcel.smsDataList.take(take).toMutableList()) else parcel
        }
        val shownItems = limitedGroups.sumOf { it.smsDataList.size }
        val height = (HEADER + limitedGroups.sumOf { GROUP_HEADER.toInt() + it.smsDataList.size * ROW_HEIGHT.toInt() } + 100)
            .coerceAtLeast(420f)
            .coerceAtMost(MAX_HEIGHT.toFloat())
            .toInt()
        val bitmap = Bitmap.createBitmap(WIDTH.toInt(), height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val colors = options.palette
        canvas.drawColor(colors.background)
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.text; textSize = 54f; typeface = android.graphics.Typeface.DEFAULT_BOLD }
        val muted = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.muted; textSize = 28f }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.text; textSize = 30f }
        val code = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.text; textSize = 42f; typeface = android.graphics.Typeface.MONOSPACE; isFakeBoldText = true }
        canvas.drawText("📦 快捷取件卡片", OUTER, 76f, title)
        canvas.drawText("${formatDate(System.currentTimeMillis())} · ${groups.sumOf { it.smsDataList.size }} 件", OUTER, 126f, muted)
        var top = HEADER
        limitedGroups.forEach { group ->
            val card = RectF(OUTER, top, WIDTH - OUTER, top + GROUP_HEADER + group.smsDataList.size * ROW_HEIGHT)
            val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.panel }
            canvas.drawRoundRect(card, 28f, 28f, panelPaint)
            val groupPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.accent; textSize = 34f; typeface = android.graphics.Typeface.DEFAULT_BOLD }
            canvas.drawText("📍 ${group.address}", OUTER + 28f, top + 58f, groupPaint)
            canvas.drawText("${group.smsDataList.size} 件", WIDTH - OUTER - 120f, top + 58f, muted)
            group.smsDataList.forEachIndexed { index, item ->
                val rowTop = top + GROUP_HEADER + index * ROW_HEIGHT
                val codeRect = RectF(OUTER + 28f, rowTop + 22f, OUTER + 410f, rowTop + 94f)
                val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.codeBackground }
                canvas.drawRoundRect(codeRect, 18f, 18f, codePaint)
                canvas.drawText(item.code.ifBlank { "未识别" }, OUTER + 50f, rowTop + 70f, code)
                val detail = listOf(item.lockerNumber, formatDate(item.sms.timestamp)).filter { it.isNotBlank() }.joinToString(" · ")
                canvas.drawText(detail, OUTER + 450f, rowTop + 64f, body)
            }
            top = card.bottom + 24f
        }
        val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.muted; textSize = 24f }
        val footerText = if (totalItems > shownItems) {
            "仅显示前 $shownItems 件（共 $totalItems 件） · 请核对地址后前往驿站取件"
        } else {
            "请核对地址后前往驿站取件"
        }
        canvas.drawText(footerText, OUTER, height - 38f, footer)
        return bitmap
    }

    fun buildTextList(parcels: List<ParcelData>, options: ExportCardOptions): String = buildString {
        parcels.filter { options.address == null || it.address == options.address }.forEach { group ->
            val items = group.smsDataList.filter { options.includeCompleted || !it.isCompleted }
            if (items.isNotEmpty()) {
                appendLine(group.address)
                items.forEach { appendLine("${it.code} ${it.lockerNumber}".trim()) }
            }
        }
    }.trim()

    fun saveToGallery(resolver: ContentResolver, bitmap: Bitmap, displayName: String): Result<android.net.Uri> = runCatching {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Parcel")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: error("无法创建相册文件")
        try {
            resolver.openOutputStream(uri)?.use { output ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) error("图片压缩失败")
            } ?: error("无法写入图片")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            uri
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    private fun formatDate(timestamp: Long): String = SimpleDateFormat("M/d HH:mm", Locale.getDefault()).format(Date(timestamp))
}
