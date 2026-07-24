package com.chenran.parcel.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.chenran.parcel.ui.components.ThemedCard
import com.chenran.parcel.ui.main.LocalAppToast
import com.chenran.parcel.ui.theme.ParcelSpacing
import com.chenran.parcel.ui.theme.parcelPalette
import com.chenran.parcel.util.exportFullBackupJson
import com.chenran.parcel.util.exportRulesToJson
import com.chenran.parcel.util.importRulesFromJson
import com.chenran.parcel.viewmodel.ParcelViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * v2.0 本地数据备份与导出（R12，对齐 Web backup 子视图）：
 * 整站配置 JSON 导出（settings+rules+mappings）+ 规则 JSON 导入导出。
 */
@Composable
fun BackupScreen(
    navController: NavController,
    viewModel: ParcelViewModel,
) {
    val palette = parcelPalette
    val context = LocalContext.current
    val showToast = LocalAppToast.current

    fun writeToUri(uri: Uri, content: String, successMsg: String) {
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(content.toByteArray())
            } ?: error("无法打开文件")
        }.onSuccess {
            showToast(successMsg)
        }.onFailure {
            showToast("导出失败：${it.message ?: "未知错误"}")
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            writeToUri(uri, exportFullBackupJson(context), "整站配置已导出")
        }
    }
    val exportRulesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            writeToUri(uri, exportRulesToJson(context), "规则已导出")
        }
    }
    val importRulesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val json = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.use { it.readText() }
                    ?: error("无法读取文件")
                importRulesFromJson(context, viewModel, json)
            }.onSuccess {
                // 导入后立即用新规则重新解析现有消息，让规则即刻生效
                viewModel.handleReceivedSms()
                showToast("规则已导入")
            }.onFailure {
                showToast("导入失败：${it.message ?: "未知错误"}")
            }
        }
    }

    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ParcelSpacing.md, vertical = ParcelSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ParcelSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = palette.textPrimary,
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "本地数据备份与导出",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
            )
        }

        // 整站备份
        ThemedCard {
            Text(
                text = "整站配置备份",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "导出主题、显示偏好、壁纸设置、自定义规则、地址映射与 AI 配置为 JSON 文件。AI Key 会以明文写入，请妥善保管（parcel_assistant_backup_$dateStr.json）",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
            Spacer(modifier = Modifier.height(ParcelSpacing.sm))
            Button(
                onClick = { backupLauncher.launch("parcel_assistant_backup_$dateStr.json") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.emerald,
                    contentColor = palette.onFab,
                ),
            ) {
                Text("导出整站配置文件 (.json)", fontWeight = FontWeight.Bold)
            }
        }

        // 规则导入导出
        ThemedCard {
            Text(
                text = "解析规则",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "规则文件可分享给其他用户一键导入",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
            Spacer(modifier = Modifier.height(ParcelSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(ParcelSpacing.sm)) {
                OutlinedButton(
                    onClick = { exportRulesLauncher.launch("parcel_rules.json") },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("导出规则")
                }
                OutlinedButton(
                    onClick = {
                        importRulesLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("导入规则")
                }
            }
        }

        // 关于入口
        OutlinedButton(
            onClick = { navController.navigate("about") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("查看关于与版本说明")
        }
    }
}
