package com.example.mqttlocationtracker.ui.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mqttlocationtracker.privacy.PrivacyManager

@Composable
fun PrivacyScreen(
    onNavigateBack: () -> Unit
) {
    val privacyManager = PrivacyManager.getInstance(androidx.compose.ui.platform.LocalContext.current)
    val privacySettings = privacyManager.getAllPrivacySettings()
    
    var locationAnonymization by remember { mutableStateOf(privacySettings.locationAnonymization) }
    var dataEncryption by remember { mutableStateOf(privacySettings.dataEncryption) }
    var autoDeleteOldData by remember { mutableStateOf(privacySettings.autoDeleteOldData) }
    var deleteOldDataDays by remember { mutableStateOf(privacySettings.deleteOldDataDays.toString()) }
    var shareAnonymousData by remember { mutableStateOf(privacySettings.shareAnonymousData) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("隐私设置") },
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
            // 位置匿名化
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "位置匿名化",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "降低位置坐标的精度以保护隐私",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Switch(
                            checked = locationAnonymization,
                            onCheckedChange = { locationAnonymization = it }
                        )
                    }
                }
            }
            
            // 数据加密
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "数据加密",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "对存储的位置数据进行加密保护",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Switch(
                            checked = dataEncryption,
                            onCheckedChange = { dataEncryption = it }
                        )
                    }
                }
            }
            
            // 自动删除旧数据
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "自动删除旧数据",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "定期删除超过指定天数的历史数据",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Switch(
                            checked = autoDeleteOldData,
                            onCheckedChange = { autoDeleteOldData = it }
                        )
                    }
                    
                    if (autoDeleteOldData) {
                        OutlinedTextField(
                            value = deleteOldDataDays,
                            onValueChange = { 
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    deleteOldDataDays = it
                                }
                            },
                            label = { Text("删除天数") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            singleLine = true
                        )
                        
                        Text(
                            text = "超过指定天数的历史位置数据将被自动删除",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            // 分享匿名数据
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "分享匿名数据",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "允许应用收集和分享匿名使用数据以改进产品",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Switch(
                            checked = shareAnonymousData,
                            onCheckedChange = { shareAnonymousData = it }
                        )
                    }
                }
            }
            
            // 保存按钮
            Button(
                onClick = {
                    // 保存设置
                    privacyManager.setLocationAnonymizationEnabled(locationAnonymization)
                    privacyManager.setDataEncryptionEnabled(dataEncryption)
                    privacyManager.setAutoDeleteOldDataEnabled(autoDeleteOldData)
                    deleteOldDataDays.toLongOrNull()?.let { days ->
                        privacyManager.setDeleteOldDataDays(days)
                    }
                    privacyManager.setShareAnonymousDataEnabled(shareAnonymousData)
                    
                    // 返回上一页
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存隐私设置")
            }
            
            // 隐私说明
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "隐私保护说明",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• 位置匿名化: 降低坐标精度，使位置信息不那么具体\n" +
                                "• 数据加密: 对本地存储的位置数据进行加密保护\n" +
                                "• 自动删除: 定期清理旧的历史数据以减少数据积累\n" +
                                "• 匿名分享: 收集使用统计数据但不包含个人身份信息\n\n" +
                                "这些设置可以帮助您更好地控制个人位置数据的使用和保护。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}