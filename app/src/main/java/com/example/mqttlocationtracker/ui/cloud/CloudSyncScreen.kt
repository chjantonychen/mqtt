package com.example.mqttlocationtracker.ui.cloud

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mqttlocationtracker.cloud.CloudSyncManager
import com.example.mqttlocationtracker.database.repository.LocationRepository

@Composable
fun CloudSyncScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val locationRepository = LocationRepository.getDatabase(context)
    val cloudSyncManager = CloudSyncManager.getInstance(context, locationRepository, androidx.lifecycle.viewmodel.compose.viewModel().coroutineScope)
    val syncStatus = cloudSyncManager.getCloudSyncStatus()
    
    var cloudSyncEnabled by remember { mutableStateOf(syncStatus.enabled) }
    var cloudEndpoint by remember { mutableStateOf(syncStatus.endpoint) }
    var apiKey by remember { mutableStateOf(if (syncStatus.isApiKeySet) "********" else "") }
    var syncInterval by remember { mutableStateOf(syncStatus.syncInterval.toString()) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("云同步设置") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // 云同步开关
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
                                text = "云同步",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "将位置数据同步到云端服务器",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Switch(
                            checked = cloudSyncEnabled,
                            onCheckedChange = { cloudSyncEnabled = it }
                        )
                    }
                }
            }
            
            // 云同步配置
            if (cloudSyncEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "云同步配置",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = cloudEndpoint,
                            onValueChange = { cloudEndpoint = it },
                            label = { Text("云服务端点") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("API密钥") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = syncInterval,
                            onValueChange = { 
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    syncInterval = it
                                }
                            },
                            label = { Text("同步间隔(毫秒)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text(
                            text = "同步间隔建议设置为300000(5分钟)或更大值",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                // 同步控制
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "同步控制",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    isSyncing = true
                                    syncResult = null
                                    
                                    // 执行同步
                                    cloudSyncManager.syncLocationsToCloud { success, message ->
                                        isSyncing = false
                                        syncResult = message
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                },
                                enabled = !isSyncing,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (isSyncing) "同步中..." else "立即同步")
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            OutlinedButton(
                                onClick = {
                                    // 测试连接
                                    Toast.makeText(context, "连接测试功能待实现", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("测试连接")
                            }
                        }
                        
                        // 同步结果
                        syncResult?.let { result ->
                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // 同步状态
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "同步状态",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "云同步状态",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Icon(
                            imageVector = if (syncStatus.enabled) Icons.Default.Cloud else Icons.Default.CloudOff,
                            contentDescription = if (syncStatus.enabled) "已启用" else "已禁用",
                            tint = if (syncStatus.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Text(
                        text = "上次同步: ${syncStatus.formatLastSyncTime()}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Text(
                        text = "同步端点: ${syncStatus.endpoint}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // 保存按钮
            Button(
                onClick = {
                    // 保存设置
                    cloudSyncManager.setCloudSyncEnabled(cloudSyncEnabled)
                    cloudSyncManager.setCloudEndpoint(cloudEndpoint)
                    
                    // 只有当API密钥不是占位符时才更新
                    if (apiKey != "********" && apiKey.isNotEmpty()) {
                        cloudSyncManager.setApiKey(apiKey)
                    }
                    
                    syncInterval.toLongOrNull()?.let { interval ->
                        cloudSyncManager.setSyncInterval(interval)
                    }
                    
                    Toast.makeText(context, "云同步设置已保存", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Icon(imageVector = Icons.Default.Cloud, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存云同步设置")
            }
            
            // 说明信息
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "云同步说明",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• 启用云同步后，位置数据将定期上传到指定的云端服务器\n" +
                                "• 需要配置正确的云服务端点和API密钥\n" +
                                "• 同步间隔决定了数据上传的频率\n" +
                                "• 可以随时手动触发立即同步\n" +
                                "• 未同步的数据会在网络恢复后自动同步\n\n" +
                                "注意：请确保云服务端点支持HTTPS以保证数据传输安全。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}