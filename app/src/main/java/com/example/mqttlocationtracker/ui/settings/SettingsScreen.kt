package com.example.mqttlocationtracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 设置界面
 */
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onNavigateBack: () -> Unit
) {
    var mqttServerUri by remember { mutableStateOf(settingsManager.getMqttServerUri()) }
    var mqttClientId by remember { mutableStateOf(settingsManager.getMqttClientId()) }
    var mqttUsername by remember { mutableStateOf(settingsManager.getMqttUsername() ?: "") }
    var mqttPassword by remember { mutableStateOf(settingsManager.getMqttPassword() ?: "") }
    var mqttUseTls by remember { mutableStateOf(settingsManager.isMqttUseTls()) }
    var mqttTopic by remember { mutableStateOf(settingsManager.getMqttTopic()) }
    var locationUpdateInterval by remember { mutableStateOf(settingsManager.getLocationUpdateInterval().toString()) }
    var locationMinDistance by remember { mutableStateOf(settingsManager.getLocationMinDistance().toString()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
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