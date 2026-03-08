package com.example.mqttlocationtracker

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mqttlocationtracker.data.LocationData
import com.example.mqttlocationtracker.mqtt.MqttManager
import com.example.mqttlocationtracker.service.LocationTrackingService
import com.example.mqttlocationtracker.ui.main.MainViewModel
import com.example.mqttlocationtracker.ui.main.MainViewModelFactory
import com.example.mqttlocationtracker.ui.settings.SettingsManager
import com.example.mqttlocationtracker.utils.PermissionManager
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    permissionManager: PermissionManager,
    locationService: LocationTrackingService?,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // 监听位置跟踪状态
    LaunchedEffect(locationService) {
        locationService?.let {
            viewModel.updateTrackingStatus(it.isTracking())
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MQTT位置跟踪器") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置"
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 连接状态
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
                        text = "连接状态",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "状态: ${uiState.connectionStatus}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveSettings()
                                viewModel.connectToMqtt()
                                
                                // 同时配置服务中的MQTT客户端
                                locationService?.configureMqtt(
                                    serverUri = uiState.serverUri,
                                    clientId = uiState.clientId,
                                    username = if (uiState.username.isNotEmpty()) uiState.username else null,
                                    password = if (uiState.password.isNotEmpty()) uiState.password else null,
                                    useTls = uiState.serverUri.startsWith("ssl") || uiState.serverUri.startsWith("tls"),
                                    topic = uiState.topic
                                )
                            },
                            enabled = !uiState.isConnected
                        ) {
                            Text("连接")
                        }
                        
                        Button(
                            onClick = {
                                viewModel.disconnectFromMqtt()
                                coroutineScope.launch {
                                    locationService?.disconnectFromMqtt()
                                }
                            },
                            enabled = uiState.isConnected
                        ) {
                            Text("断开连接")
                        }
                    }
                }
            }
            
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
                        value = uiState.serverUri,
                        onValueChange = { viewModel.updateServerUri(it) },
                        label = { Text("服务器URI") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = uiState.clientId,
                        onValueChange = { viewModel.updateClientId(it) },
                        label = { Text("客户端ID") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = { viewModel.updateUsername(it) },
                        label = { Text("用户名(可选)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = { Text("密码(可选)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = uiState.topic,
                        onValueChange = { viewModel.updateTopic(it) },
                        label = { Text("主题") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }
            
            // 位置跟踪控制
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
                        text = "位置跟踪",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "跟踪状态: ${if (uiState.isTracking) "运行中" else "已停止"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                // 启动位置跟踪服务
                                startLocationTrackingService(context)
                                
                                // 启动位置跟踪
                                locationService?.startTracking()
                                viewModel.updateTrackingStatus(locationService?.isTracking() ?: false)
                            },
                            enabled = uiState.isConnected && locationService != null
                        ) {
                            Text("开始跟踪")
                        }
                        
                        Button(
                            onClick = {
                                // 停止位置跟踪
                                locationService?.stopTracking()
                                viewModel.updateTrackingStatus(locationService?.isTracking() ?: false)
                            },
                            enabled = uiState.isTracking && locationService != null
                        ) {
                            Text("停止跟踪")
                        }
                    }
                }
            }
            
            // 实时位置信息
            if (uiState.currentLocation != null) {
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
                            text = "实时位置",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "纬度: ${uiState.currentLocation?.latitude}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "经度: ${uiState.currentLocation?.longitude}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "精度: ${uiState.currentLocation?.accuracy}米",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "时间: ${uiState.currentLocation?.timestamp}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // 历史数据
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
                        text = "历史数据",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Button(
                        onClick = {
                            // 查看历史位置数据
                            // 这里可以导航到历史数据页面
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("查看历史位置")
                    }
                }
            }
        }
    }
}

/**
 * 启动位置跟踪服务
 */
private fun startLocationTrackingService(context: Context) {
    val intent = Intent(context, LocationTrackingService::class.java)
    context.startService(intent)
}