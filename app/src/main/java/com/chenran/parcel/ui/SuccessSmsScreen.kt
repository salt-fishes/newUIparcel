package com.chenran.parcel.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chenran.parcel.ui.components.TagDialog
import com.chenran.parcel.ui.components.ParcelCard
import com.chenran.parcel.ui.components.parcelOnCardColor
import com.chenran.parcel.ui.components.parcelOnCardVariantColor
import com.chenran.parcel.ui.components.parcelSheetColor
import com.chenran.parcel.util.removeCustomSms
import com.chenran.parcel.util.getAddressMappings
import com.chenran.parcel.util.saveAddressMapping
import com.chenran.parcel.util.removeAddressMapping
import com.chenran.parcel.viewmodel.ParcelViewModel
import java.net.URLEncoder
import com.chenran.parcel.util.dateToString
import com.chenran.parcel.util.isCustomSms


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessSmsScreen(
    viewModel: ParcelViewModel,
    navController: NavController,
    isSeniorMode: Boolean = false,
    readAndParseSms: () -> Unit = {},
) {
    val context = LocalContext.current
    val successSmsData by viewModel.successSmsData.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var currentAddress by remember { mutableStateOf("") }
    var currentTag by remember { mutableStateOf("") }


    // 地址标签映射异步加载一次，随数据刷新重载，避免每个列表项组合时同步读盘
    val addressMappings by produceState(initialValue = emptyMap<String, String>(), successSmsData) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            getAddressMappings(context)
        }
    }

    fun getTagForAddress(addr: String): String? = addressMappings[addr]

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text("已解析${successSmsData.size}条短信")
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                modifier = Modifier.size(if (isSeniorMode) 36.dp else 24.dp)
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(successSmsData) { data ->
                    val tag = getTagForAddress(data.address)
                    ParcelCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            SelectionContainer {
                                Text(
                                    text = data.sms.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = parcelOnCardColor()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dateToString(data.sms.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = parcelOnCardVariantColor()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "地址: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = parcelOnCardColor()
                                )
                                SelectionContainer {
                                    Text(
                                        text = data.address,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "取件码: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = parcelOnCardColor()
                                )
                                SelectionContainer {
                                    Text(
                                        text = data.code,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }

//                            Spacer(modifier = Modifier.height(6.dp))

                            if (tag != null) {
                                Surface(
                                    onClick = {
                                        currentAddress = data.address
                                        currentTag = tag
                                        showEditDialog = true
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ) {
                                    Text(
                                        text = tag,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 4.dp
                                        ),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            } else {
                                Surface(
                                    onClick = {
                                        currentAddress = data.address
                                        currentTag = ""
                                        showEditDialog = true
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .size(if (isSeniorMode) 21.dp else 14.dp),
                                        )
                                        Text(
                                            text = "添加地址标签",
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            ),
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val encodedMsg = URLEncoder.encode(data.sms.body, "UTF-8")
                                        navController.navigate("add_rule?message=${encodedMsg}")
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "添加解析规则",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }

                                if (isCustomSms(data.sms)) {
                                    OutlinedButton(
                                        onClick = {
                                            removeCustomSms(context, data.sms.id)
                                            readAndParseSms()
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "删除",
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .size(18.dp)
                                        )
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        if (showEditDialog) {
            TagDialog(
                title = "设置地址标签",
                currentAddress = currentAddress,
                currentTag = currentTag,
                onDismiss = { showEditDialog = false },
                onConfirm = { tag ->
                    if (tag.isNotBlank()) {
                        saveAddressMapping(context, currentAddress, tag)
                        readAndParseSms()
                    }
                    showEditDialog = false
                },
                onRemove = {
                    removeAddressMapping(context, currentAddress)
                    readAndParseSms()
                    showEditDialog = false
                }
            )
        }
    }
}
