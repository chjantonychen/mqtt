package com.example.mqttlocationtracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.mqttlocationtracker.data.LocationData
import com.example.mqttlocationtracker.mqtt.MqttManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private lateinit var mqttManager: MqttManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化MQTT管理器
        mqttManager = MqttManager(lifecycleScope)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MqttDemoScreen(mqttManager, this)
                }
            }
        }
    }
}

@Composable
fun MqttDemoScreen(mqttManager: MqttManager, activity: MainActivity) {
    var isConnected by remember { mutableStateOf(false) }
    var serverUri by remember { mutableStateOf("tcp://broker.hivemq.com:1883") }
    var clientId by remember { mutableStateOf("android_client_${System.currentTimeMillis()}") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("location/tracker") }
    var message by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("未连接") }
    
    // 设置连接监听器
    DisposableEffect(mqttManager) {
        mqttManager.setConnectionListener(object : MqttManager.ConnectionListener {
            override fun onConnected() {
                isConnected = true
                statusText = "已连接"
            }
            
            override fun onDisconnected() {
                isConnected = false
                statusText = "已断开连接"
            }
            
            override fun onConnectionLost(cause: Throwable?) {
                isConnected = false
                statusText = "连接丢失: ${cause?.message ?: "未知错误"}"
            }
            
            override fun onReconnecting(attempt: Int, delayMs: Long) {
                statusText = "重连中... (尝试 $attempt, 延迟 ${delayMs}ms)"
            }
        })
        
        onDispose {
            // 清理资源
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MQTT位置跟踪器演示",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "状态: $statusText",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
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
                .padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    // 配置MQTT客户端
                    mqttManager.configure(
                        serverUri = serverUri,
                        clientId = clientId,
                        username = if (username.isNotEmpty()) username else null,
                        password = if (password.isNotEmpty()) password else null,
                        useTls = serverUri.startsWith("ssl") || serverUri.startsWith("tls")
                    )
                    
                    // 连接
                    activity.lifecycleScope.launch {
                        try {
                            mqttManager.connect()
                        } catch (e: Exception) {
                            statusText = "连接失败: ${e.message}"
                        }
                    }
                },
                enabled = !isConnected
            ) {
                Text("连接")
            }
            
            Button(
                onClick = {
                    activity.lifecycleScope.launch {
                        try {
                            mqttManager.disconnect()
                        } catch (e: Exception) {
                            statusText = "断开连接失败: ${e.message}"
                        }
                    }
                },
                enabled = isConnected
            ) {
                Text("断开连接")
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text(
            text = "发布消息",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = topic,
            onValueChange = { topic = it },
            label = { Text("主题") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("消息内容") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        Button(
            onClick = {
                activity.lifecycleScope.launch {
                    try {
                        mqttManager.publish(topic, message)
                        statusText = "消息已发布"
                    } catch (e: Exception) {
                        statusText = "发布失败: ${e.message}"
                    }
                }
            },
            enabled = isConnected,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("发布消息")
        }
        
        Button(
            onClick = {
                // 创建示例位置数据
                val locationData = LocationData(
                    latitude = 39.9042,
                    longitude = 116.4074,
                    accuracy = 10.0f
                )
                
                activity.lifecycleScope.launch {
                    try {
                        mqttManager.publish(topic, locationData.toJson())
                        statusText = "位置数据已发布"
                    } catch (e: Exception) {
                        statusText = "发布位置数据失败: ${e.message}"
                    }
                }
            },
            enabled = isConnected
        ) {
            Text("发布示例位置数据")
        }
    }
}