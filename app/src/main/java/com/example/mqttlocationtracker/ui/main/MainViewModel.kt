package com.example.mqttlocationtracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mqttlocationtracker.data.LocationData
import com.example.mqttlocationtracker.mqtt.MqttManager
import com.example.mqttlocationtracker.ui.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 主界面ViewModel
 */
class MainViewModel(
    private val mqttManager: MqttManager,
    private val settingsManager: SettingsManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        // 初始化MQTT连接监听器
        mqttManager.setConnectionListener(object : MqttManager.ConnectionListener {
            override fun onConnected() {
                _uiState.value = _uiState.value.copy(
                    isConnected = true,
                    connectionStatus = "已连接"
                )
            }
            
            override fun onDisconnected() {
                _uiState.value = _uiState.value.copy(
                    isConnected = false,
                    connectionStatus = "已断开连接"
                )
            }
            
            override fun onConnectionLost(cause: Throwable?) {
                _uiState.value = _uiState.value.copy(
                    isConnected = false,
                    connectionStatus = "连接丢失: ${cause?.message ?: "未知错误"}"
                )
            }
            
            override fun onReconnecting(attempt: Int, delayMs: Long) {
                _uiState.value = _uiState.value.copy(
                    connectionStatus = "重连中... (尝试 $attempt, 延迟 ${delayMs}ms)"
                )
            }
        })
        
        // 加载保存的设置
        loadSettings()
    }
    
    /**
     * 加载保存的设置
     */
    private fun loadSettings() {
        _uiState.value = _uiState.value.copy(
            serverUri = settingsManager.getMqttServerUri(),
            clientId = settingsManager.getMqttClientId(),
            username = settingsManager.getMqttUsername() ?: "",
            password = settingsManager.getMqttPassword() ?: "",
            topic = settingsManager.getMqttTopic()
        )
    }
    
    /**
     * 更新当前定位信息
     */
    fun updateCurrentLocation(locationData: LocationData) {
        _uiState.value = _uiState.value.copy(
            currentLocation = locationData
        )
    }
    
    /**
     * 更新跟踪状态
     */
    fun updateTrackingStatus(isTracking: Boolean) {
        _uiState.value = _uiState.value.copy(
            isTracking = isTracking
        )
    }
    
    /**
     * 连接到MQTT服务器
     */
    fun connectToMqtt() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                mqttManager.configure(
                    serverUri = currentState.serverUri,
                    clientId = currentState.clientId,
                    username = if (currentState.username.isNotEmpty()) currentState.username else null,
                    password = if (currentState.password.isNotEmpty()) currentState.password else null,
                    useTls = currentState.serverUri.startsWith("ssl") || currentState.serverUri.startsWith("tls")
                )
                
                mqttManager.connect()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "连接失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 断开MQTT连接
     */
    fun disconnectFromMqtt() {
        mqttManager.disconnect()
    }
    
    /**
     * 测试MQTT连接
     */
    fun testMqttConnection() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = true,
                    connectionStatus = "测试连接中..."
                )
                
                // 获取当前设置
                val serverUri = _uiState.value.serverUri
                val clientId = _uiState.value.clientId
                val username = if (_uiState.value.username.isNotEmpty()) _uiState.value.username else null
                val password = if (_uiState.value.password.isNotEmpty()) _uiState.value.password else null
                val useTls = serverUri.startsWith("ssl") || serverUri.startsWith("tls")
                
                // 尝试连接
                val future = mqttManager.connect(serverUri, clientId, username, password, useTls)
                
                // 等待连接结果（最多等待5秒）
                future.get(5, java.util.concurrent.TimeUnit.SECONDS)
                
                // 连接成功
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    connectionStatus = "连接测试成功"
                )
                
                // 断开测试连接
                mqttManager.disconnect()
            } catch (e: Exception) {
                // 连接失败
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    connectionStatus = "连接测试失败: ${e.message}"
                )
            }
        }
    }
        }
    }
    
    /**
     * 更新服务器URI
     */
    fun updateServerUri(uri: String) {
        _uiState.value = _uiState.value.copy(
            serverUri = uri
        )
    }
    
    /**
     * 更新客户端ID
     */
    fun updateClientId(clientId: String) {
        _uiState.value = _uiState.value.copy(
            clientId = clientId
        )
    }
    
    /**
     * 更新用户名
     */
    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username
        )
    }
    
    /**
     * 更新密码
     */
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password
        )
    }
    
    /**
     * 更新主题
     */
    fun updateTopic(topic: String) {
        _uiState.value = _uiState.value.copy(
            topic = topic
        )
    }
    
    /**
     * 保存设置
     */
    fun saveSettings() {
        val currentState = _uiState.value
        settingsManager.setMqttServerUri(currentState.serverUri)
        settingsManager.setMqttClientId(currentState.clientId)
        settingsManager.setMqttUsername(if (currentState.username.isNotEmpty()) currentState.username else null)
        settingsManager.setMqttPassword(if (currentState.password.isNotEmpty()) currentState.password else null)
        settingsManager.setMqttTopic(currentState.topic)
    }
    
    /**
     * 清除错误信息
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
}