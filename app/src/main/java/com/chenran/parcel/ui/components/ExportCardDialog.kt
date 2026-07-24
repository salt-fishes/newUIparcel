package com.chenran.parcel.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.chenran.parcel.model.ParcelData
import com.chenran.parcel.ui.main.LocalAppToast
import com.chenran.parcel.util.ExportCardOptions
import com.chenran.parcel.util.ExportCardPalette
import com.chenran.parcel.util.ExportCardRenderer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportCardDialog(
    context: Context,
    parcels: List<ParcelData>,
    onDismiss: () -> Unit,
) {
    val toast = LocalAppToast.current
    val addresses = remember(parcels) { parcels.map { it.address }.distinct() }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var palette by remember { mutableStateOf(ExportCardPalette.DARK) }
    var includeCompleted by remember { mutableStateOf(false) }
    var addressMenu by remember { mutableStateOf(false) }
    var paletteMenu by remember { mutableStateOf(false) }
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    val scope = rememberCoroutineScope()

    fun options() = ExportCardOptions(palette, selectedAddress, includeCompleted)
    LaunchedEffect(selectedAddress, palette, includeCompleted, parcels) {
        bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            ExportCardRenderer.render(parcels, options())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导出取件图片卡片", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = addressMenu, onExpandedChange = { addressMenu = !addressMenu }) {
                    OutlinedTextField(
                        value = selectedAddress ?: "全部地址",
                        onValueChange = {}, readOnly = true,
                        label = { Text("自提点范围") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(addressMenu) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded = addressMenu, onDismissRequest = { addressMenu = false }) {
                        DropdownMenuItem(text = { Text("全部地址") }, onClick = { selectedAddress = null; addressMenu = false })
                        addresses.forEach { address -> DropdownMenuItem(text = { Text(address) }, onClick = { selectedAddress = address; addressMenu = false }) }
                    }
                }
                ExposedDropdownMenuBox(expanded = paletteMenu, onExpandedChange = { paletteMenu = !paletteMenu }) {
                    OutlinedTextField(
                        value = palette.name, onValueChange = {}, readOnly = true,
                        label = { Text("卡片配色") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(paletteMenu) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded = paletteMenu, onDismissRequest = { paletteMenu = false }) {
                        ExportCardPalette.entries.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { palette = item; paletteMenu = false }) }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeCompleted, onCheckedChange = { includeCompleted = it })
                    Text("包含已取件项目", style = MaterialTheme.typography.bodyMedium)
                }
                bitmap?.let { Image(it.asImageBitmap(), contentDescription = "取件卡片预览", modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val text = ExportCardRenderer.buildTextList(parcels, options())
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("取件清单", text))
                    toast("取件清单已复制")
                }) { Text("复制清单") }
                Button(onClick = {
                    val name = "取件码卡片_${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.png"
                    val target = bitmap
                    if (target == null) {
                        toast("图片尚未生成")
                    } else {
                        scope.launch {
                            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                ExportCardRenderer.saveToGallery(context.contentResolver, target, name)
                            }
                            result.onSuccess { toast("图片已保存到相册") }.onFailure { toast("保存失败：${it.message ?: "未知错误"}") }
                        }
                    }
                }) { Text("保存 PNG") }
            }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("关闭") } },
    )
}
