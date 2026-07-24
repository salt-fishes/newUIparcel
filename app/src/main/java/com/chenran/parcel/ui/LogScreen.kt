package com.chenran.parcel.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chenran.parcel.ui.components.ParcelCard
import com.chenran.parcel.ui.components.parcelOnCardColor
import com.chenran.parcel.ui.components.parcelOnCardVariantColor
import com.chenran.parcel.ui.components.parcelSheetColor
import com.chenran.parcel.util.clearAllLogs
import com.chenran.parcel.util.getLogs
import com.chenran.parcel.util.LogEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(navController: NavController) {
    val context = LocalContext.current
    val selectedDay = remember { mutableStateOf<Long?>(null) }
    val sdfDay = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    var logs by remember { mutableStateOf(emptyList<LogEntry>()) }
    val clipboard = LocalClipboardManager.current
    var searchQuery by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredLogs = remember(logs, searchQuery) {
        if (searchQuery.isBlank()) logs
        else logs.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(selectedDay.value) {
        logs = withContext(Dispatchers.IO) { getLogs(context, selectedDay.value) }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("确认清除", style = MaterialTheme.typography.titleLarge) },
            text = { Text("确定要清除所有日志吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    clearAllLogs(context)
                    logs = emptyList()
                    showClearDialog = false
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("日志") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { showClearDialog = true }) { Text("清除全部") }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
        ) {
            // 跟踪日期选择弹窗，组合销毁时随之一同关闭，防止窗口泄漏
            var datePickerDialog by remember { mutableStateOf<DatePickerDialog?>(null) }
            androidx.compose.runtime.DisposableEffect(Unit) {
                onDispose { datePickerDialog?.dismiss(); datePickerDialog = null }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = {
                    val cal = Calendar.getInstance()
                    val dialog = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val c = Calendar.getInstance()
                            c.set(year, month, dayOfMonth, 0, 0, 0)
                            c.set(Calendar.MILLISECOND, 0)
                            selectedDay.value = c.timeInMillis
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                    datePickerDialog = dialog
                    dialog.show()
                }) { Text("选择日期") }
                TextButton(onClick = { selectedDay.value = null }) { Text("显示全部") }
                TextButton(onClick = {
                    val content = filteredLogs.joinToString(separator = "\n\n") { entry ->
                        val dayStr = sdfDay.format(Date(entry.timestamp))
                        val timeStr = sdfTime.format(Date(entry.timestamp))
                        val ver = entry.version.ifBlank { "" }
                        "${if (ver.isNotBlank()) "[v$ver] " else ""}$dayStr $timeStr\n${entry.text}"
                    }
                    clipboard.setText(AnnotatedString(content))
                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                }) { Text("复制") }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索日志...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            ParcelCard(
                modifier = Modifier.fillMaxSize(),
            ) {
                if (filteredLogs.isEmpty()) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        Text(text = if (logs.isEmpty()) "暂无日志" else "无匹配结果", style = MaterialTheme.typography.bodyMedium, color = parcelOnCardColor())
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(items = filteredLogs, key = { "${it.timestamp}_${it.text.hashCode()}" }) { entry ->
                            val dayStr = sdfDay.format(Date(entry.timestamp))
                            val timeStr = sdfTime.format(Date(entry.timestamp))
                            val ver = entry.version.ifBlank { "" }
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Text(text = (if (ver.isNotBlank()) "[v$ver] " else "") + "$dayStr $timeStr", style = MaterialTheme.typography.bodySmall, color = parcelOnCardVariantColor())
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = entry.text, style = MaterialTheme.typography.bodyMedium, color = parcelOnCardColor())
                            }
                        }
                    }
                }
            }
        }
    }
}
