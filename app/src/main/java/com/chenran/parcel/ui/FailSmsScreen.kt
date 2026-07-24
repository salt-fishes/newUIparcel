package com.chenran.parcel.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chenran.parcel.ui.components.ParcelCard
import com.chenran.parcel.ui.components.parcelOnCardVariantColor
import com.chenran.parcel.util.dateToString
import com.chenran.parcel.util.getAiConfig
import com.chenran.parcel.util.isCustomSms
import com.chenran.parcel.util.removeCustomSms
import com.chenran.parcel.util.ai.AiParseScheduler
import com.chenran.parcel.util.ai.ParseStatus
import com.chenran.parcel.viewmodel.ParcelViewModel
import java.net.URLEncoder

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FailSmsScreen(viewModel: ParcelViewModel, navController: NavController, isSeniorMode: Boolean = false, readAndParseSms: () -> Unit = {}) {
    val context = LocalContext.current
    val failSmsData by viewModel.failedMessages.collectAsState()
    // 字号统一交 SeniorTypography 放大，界面不再手动分支
    val textStyle = MaterialTheme.typography.bodyMedium
    val captionStyle = MaterialTheme.typography.bodySmall

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("解析失败${failSmsData.size}条短信") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(failSmsData, key = { "${it.message.source}_${it.message.id}_${it.message.timestamp}" }) { item ->
                val message = item.message
                ParcelCard(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        SelectionContainer {
                            Text(
                                text = message.body,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = textStyle,
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dateToString(message.timestamp),
                            style = captionStyle,
                            color = parcelOnCardVariantColor(),
                        )
                        if (item.showAiStatus && item.aiStatus != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (item.aiStatus) {
                                    ParseStatus.PENDING -> "AI：等待解析"
                                    ParseStatus.PROCESSING -> "AI：正在解析"
                                    ParseStatus.RETRY_WAIT -> "AI：网络异常，等待自动重试"
                                    ParseStatus.FAILED -> "AI：${item.aiError.ifBlank { "解析失败" }}"
                                    ParseStatus.SUCCESS -> "AI：未识别为有效取件短信"
                                },
                                style = captionStyle,
                                color = if (item.aiStatus == ParseStatus.FAILED) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    parcelOnCardVariantColor()
                                },
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = {
                                    val encodedMsg = URLEncoder.encode(message.body, "UTF-8")
                                    navController.navigate("add_rule?message=${encodedMsg}")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                            ) {
                                Text(
                                    text = "添加解析规则",
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            if (item.showAiStatus) {
                                OutlinedButton(
                                    onClick = {
                                        if (AiParseScheduler.retryNow(context, message, getAiConfig(context))) {
                                            readAndParseSms()
                                        }
                                    },
                                    enabled = item.canRetry,
                                    modifier = Modifier.heightIn(min = 48.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "重新解析",
                                        modifier = Modifier.size(18.dp),
                                    )
                                    if (isSeniorMode) {
                                        Text("重试", modifier = Modifier.padding(start = 4.dp))
                                    }
                                }
                            }

                            if (isCustomSms(message)) {
                                OutlinedButton(
                                    onClick = {
                                        removeCustomSms(context, message.id)
                                        readAndParseSms()
                                    },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除",
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
