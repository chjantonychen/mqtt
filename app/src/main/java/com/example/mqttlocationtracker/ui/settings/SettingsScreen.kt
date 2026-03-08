package com.example.mqttlocationtracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mqttlocationtracker.utils.Logger

@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onNavigateBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToCloudSync: () -> Unit,
    onNavigateToBackupRestore: () -> Unit
) {
    var serverUri by remember { mutableStateOf(settingsManager.getServerUri()) }
    var clientId by remember { mutableStateOf(settingsManager.getClientId()) }
    var username by remember { mutableStateOf(settingsManager.getUsername()) }
    var password by remember { mutableStateOf(settingsManager.getPassword()) }
    var topic by remember { mutableStateOf(settingsManager.getTopic()) }
    var dataRetentionDays by remember { mutableStateOf(settingsManager.getDataRetentionDays()) }
    var loggingEnabled by remember { mutableStateOf(settingsManager.isLoggingEnabled()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
            // MQTT配置
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "MQTT配置",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = serverUri,
                        onValueChange = { serverUri = it },
                        label = { Text("服务器URI") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = clientId,
                        onValueChange = { clientId = it },
                        label = { Text("客户端ID") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("用户名(可选)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码(可选)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("主题") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }
            
            // 数据清理配置
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "数据清理配置",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "数据保留天数",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Slider(
                            value = dataRetentionDays.toFloat(),
                            onValueChange = { dataRetentionDays = it.toLong() },
                            valueRange = 1f..365f,
                            steps = 363,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${dataRetentionDays}天",
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    
                    Text(
                        text = "应用将自动清理超过设定天数的历史位置数据",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            
            // 隐私配置
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "隐私配置",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Button(
                        onClick = onNavigateToPrivacy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text("隐私保护设置")
                    }
                    
                    Button(
                        onClick = onNavigateToCloudSync,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text("云同步设置")
                    }
                    
                    Button(
                        onClick = onNavigateToBackupRestore,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("备份与恢复")
                    }
                }
            }
            
            // 日志配置
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "日志配置",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "启用日志记录",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = loggingEnabled,
                            onCheckedChange = { loggingEnabled = it }
                        )
                    }
                }
            }
            
            // 保存按钮
            Button(
                onClick = {
                    settingsManager.setServerUri(serverUri)
                    settingsManager.setClientId(clientId)
                    settingsManager.setUsername(username)
                    settingsManager.setPassword(password)
                    settingsManager.setTopic(topic)
                    settingsManager.setDataRetentionDays(dataRetentionDays)
                    settingsManager.setLoggingEnabled(loggingEnabled)
                    
                    // 应用日志设置
                    Logger.setLoggingEnabled(loggingEnabled)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("保存设置")
            }
        }
    }
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
            // MQTT设置部分
            Text(
                text = "MQTT设置",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = mqttServerUri,
                onValueChange = { mqttServerUri = it },
                label = { Text("服务器URI") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = mqttClientId,
                onValueChange = { mqttClientId = it },
                label = { Text("客户端ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = mqttUsername,
                onValueChange = { mqttUsername = it },
                label = { Text("用户名(可选)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = mqttPassword,
                onValueChange = { mqttPassword = it },
                label = { Text("密码(可选)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Checkbox(
                    checked = mqttUseTls,
                    onCheckedChange = { mqttUseTls = it }
                )
                Text("使用TLS/SSL连接")
            }
            
            OutlinedTextField(
                value = mqttTopic,
                onValueChange = { mqttTopic = it },
                label = { Text("主题") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // 位置跟踪设置部分
            Text(
                text = "位置跟踪设置",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = locationUpdateInterval,
                onValueChange = { 
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        locationUpdateInterval = it
                    }
                },
                label = { Text("更新间隔(毫秒)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = locationMinDistance,
                onValueChange = { 
                    if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) {
                        locationMinDistance = it
                    }
                },
                label = { Text("最小更新距离(米)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // 保存按钮
            Button(
                onClick = {
                    // 保存设置
                    settingsManager.setMqttServerUri(mqttServerUri)
                    settingsManager.setMqttClientId(mqttClientId)
                    settingsManager.setMqttUsername(if (mqttUsername.isNotEmpty()) mqttUsername else null)
                    settingsManager.setMqttPassword(if (mqttPassword.isNotEmpty()) mqttPassword else null)
                    settingsManager.setMqttUseTls(mqttUseTls)
                    settingsManager.setMqttTopic(mqttTopic)
                    
                    // 保存位置设置
                    locationUpdateInterval.toLongOrNull()?.let { interval ->
                        settingsManager.setLocationUpdateInterval(interval)
                    }
                    
                    locationMinDistance.toFloatOrNull()?.let { distance ->
                        settingsManager.setLocationMinDistance(distance)
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("保存设置")
            }
        }
    }
}