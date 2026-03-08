package com.example.mqttlocationtracker.ui.share

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mqttlocationtracker.data.LocationData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ShareLocationScreen(
    currentLocation: LocationData?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var shareFormat by remember { mutableStateOf("json") }
    var includeTimestamp by remember { mutableStateOf(true) }
    var customMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分享位置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 当前位置信息
            currentLocation?.let { location ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "当前位置",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "纬度: ${location.latitude}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = "经度: ${location.longitude}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        location.accuracy?.let {
                            Text(
                                text = "精度: ${it}米",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Text(
                            text = "时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(location.timestamp))}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } ?: run {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "无法获取当前位置",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "请确保位置服务已开启并授予相应权限",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 分享选项
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "分享选项",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // 格式选择
                    Text(
                        text = "分享格式",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = shareFormat == "json",
                            onClick = { shareFormat = "json" },
                            label = { Text("JSON") }
                        )

                        FilterChip(
                            selected = shareFormat == "csv",
                            onClick = { shareFormat = "csv" },
                            label = { Text("CSV") }
                        )

                        FilterChip(
                            selected = shareFormat == "text",
                            onClick = { shareFormat = "text" },
                            label = { Text("纯文本") }
                        )
                        
                        FilterChip(
                            selected = shareFormat == "map",
                            onClick = { shareFormat = "map" },
                            label = { Text("地图链接") }
                        )
                    }

                    // 自定义消息
                    Text(
                        text = "自定义消息",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = customMessage,
                        onValueChange = { customMessage = it },
                        label = { Text("添加备注信息（可选）") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // 分享按钮
                    Button(
                        onClick = {
                            currentLocation?.let { location ->
                                shareLocationData(
                                    context = context,
                                    locationData = location,
                                    format = shareFormat,
                                    customMessage = customMessage
                                )
                            } ?: run {
                                Toast.makeText(context, "无法分享位置：当前位置不可用", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentLocation != null
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("分享位置")
                    }
                }
            }

            // 使用说明
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "使用说明",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• JSON格式: 结构化的数据格式，适合开发者使用\n" +
                                "• CSV格式: 逗号分隔值格式，可在表格软件中打开\n" +
                                "• 纯文本格式: 易读的文本格式，适合普通用户\n" +
                                "• 地图链接: 生成可点击的Google Maps链接\n" +
                                "• 可以添加自定义备注信息一起分享",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * 分享位置数据
 */
private fun shareLocationData(
    context: android.content.Context,
    locationData: LocationData,
    format: String,
    customMessage: String
) {
    try {
        val shareContent = when (format) {
            "json" -> {
                val jsonContent = locationData.toJson()
                if (customMessage.isNotBlank()) {
                    "$customMessage\n\n位置信息 (JSON):\n$jsonContent"
                } else {
                    "位置信息 (JSON):\n$jsonContent"
                }
            }
            "csv" -> {
                val csvHeader = locationData.getCsvHeader()
                val csvRow = locationData.toCsv()
                if (customMessage.isNotBlank()) {
                    "$customMessage\n\n位置信息 (CSV):\n$csvHeader\n$csvRow"
                } else {
                    "位置信息 (CSV):\n$csvHeader\n$csvRow"
                }
            }
            "text" -> {
                val textContent = "纬度: ${locationData.latitude}\n" +
                        "经度: ${locationData.longitude}\n" +
                        "时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(locationData.timestamp))}" +
                        locationData.accuracy?.let { "\n精度: ${it}米" } +
                        locationData.altitude?.let { "\n海拔: ${it}米" } +
                        locationData.speed?.let { "\n速度: ${it} m/s" }
                if (customMessage.isNotBlank()) {
                    "$customMessage\n\n位置信息:\n$textContent"
                } else {
                    "位置信息:\n$textContent"
                }
            }
            "map" -> {
                // 生成Google Maps链接
                val mapUrl = "https://www.google.com/maps?q=${locationData.latitude},${locationData.longitude}"
                val mapContent = "查看我的位置:\n$mapUrl" +
                        "\n\n纬度: ${locationData.latitude}\n经度: ${locationData.longitude}" +
                        locationData.accuracy?.let { "\n精度: ${it}米" } ?: ""
                if (customMessage.isNotBlank()) {
                    "$customMessage\n\n$mapContent"
                } else {
                    mapContent
                }
            }
            else -> {
                "位置信息:\n纬度: ${locationData.latitude}, 经度: ${locationData.longitude}"
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareContent)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "分享位置信息")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}