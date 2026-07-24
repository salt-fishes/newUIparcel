package com.chenran.parcel.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chenran.parcel.ui.components.ParcelCard
import com.chenran.parcel.util.ai.AiClient
import com.chenran.parcel.util.ai.AiConfig
import com.chenran.parcel.util.ai.AI_PROVIDER_PRESETS
import com.chenran.parcel.util.ai.providerById
import com.chenran.parcel.util.getAiConfig
import com.chenran.parcel.util.saveAiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var config by remember { mutableStateOf(getAiConfig(context)) }
    var providerExpanded by remember { mutableStateOf(false) }
    var showKey by remember { mutableStateOf(false) }
    var models by remember { mutableStateOf(config.savedModels) }
    var status by remember { mutableStateOf("") }
    var testing by remember { mutableStateOf(false) }

    var persistJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val latestConfig by androidx.compose.runtime.rememberUpdatedState(config)

    fun persistNow(next: AiConfig) {
        val ok = saveAiConfig(context, next)
        if (!ok) status = "保存失败，请检查设备存储加密状态"
    }

    fun save(next: AiConfig) {
        config = next
        // 防抖落盘：避免每次按键都触发 Keystore 加密 + JSON 写盘
        persistJob?.cancel()
        persistJob = scope.launch {
            kotlinx.coroutines.delay(400)
            withContext(Dispatchers.IO) { persistNow(next) }
        }
    }

    // 离开页面时立即落盘未保存的配置，防止防抖窗口内丢失输入
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            persistJob?.cancel()
            kotlinx.coroutines.runBlocking(Dispatchers.IO) { persistNow(latestConfig) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 助手配置") },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ParcelCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("AI 大模型智能解析", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("仅支持 HTTPS；自动接收短信仍默认使用正则解析。", style = MaterialTheme.typography.bodySmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (config.enabled) "已启用" else "未启用", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = config.enabled, onCheckedChange = { save(config.copy(enabled = it)) })
                    }
                }
            }
            ExposedDropdownMenuBox(expanded = providerExpanded, onExpandedChange = { providerExpanded = !providerExpanded }) {
                OutlinedTextField(
                    value = providerById(config.providerId).name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("服务商") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(providerExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(expanded = providerExpanded, onDismissRequest = { providerExpanded = false }) {
                    AI_PROVIDER_PRESETS.forEach { preset ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(preset.name) },
                            onClick = {
                                save(config.copy(providerId = preset.id, apiUrl = preset.apiUrl, modelName = preset.defaultModel, savedModels = preset.popularModels))
                                models = preset.popularModels
                                providerExpanded = false
                            },
                        )
                    }
                }
            }
            OutlinedTextField(config.apiUrl, { save(config.copy(apiUrl = it)) }, label = { Text("API 地址（HTTPS）") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = config.apiKey,
                onValueChange = { save(config.copy(apiKey = it)) },
                label = { Text("API Key") },
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { showKey = !showKey }) { Icon(Icons.Filled.Visibility, "显示或隐藏密钥") } },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(config.modelName, { save(config.copy(modelName = it)) }, label = { Text("模型") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(config.imageApiUrl, { save(config.copy(imageApiUrl = it)) }, label = { Text("图片生成地址（可选）") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(config.systemPrompt, { save(config.copy(systemPrompt = it)) }, label = { Text("系统提示词") }, minLines = 4, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = !testing, onClick = {
                    testing = true
                    status = "正在测试连通性..."
                    scope.launch {
                        val result = withContext(Dispatchers.IO) { AiClient.testConnectivity(config) }
                        status = result.message
                        testing = false
                    }
                }) { Text("连通测试") }
                Button(onClick = {
                    status = "正在拉取模型..."
                    scope.launch {
                        val result = withContext(Dispatchers.IO) { AiClient.fetchAvailableModels(config) }
                        result.onSuccess { loaded -> models = loaded; save(config.copy(savedModels = loaded)); status = "已拉取 ${loaded.size} 个模型" }
                            .onFailure { status = it.message ?: "拉取失败" }
                    }
                }) { Icon(Icons.Filled.Refresh, null); Text("拉取模型") }
            }
            if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall)
            if (models.isNotEmpty()) {
                Text("模型列表", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                models.forEach { model ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(model, style = MaterialTheme.typography.bodyMedium)
                        Button(onClick = { save(config.copy(modelName = model)) }) { Text("选择") }
                    }
                }
            }
        }
    }
}
