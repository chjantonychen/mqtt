package com.example.mqttlocationtracker.mqtt

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import com.example.mqttlocationtracker.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory
import kotlin.math.min

/**
 * MQTT客户端管理类，支持认证和自动重连
 */
class MqttManager(private val coroutineScope: CoroutineScope) {
    
    private var client: Mqtt3Client? = null
    private var serverUri: String = ""
    private var clientId: String = ""
    private var username: String? = null
    private var password: String? = null
    private var useTls: Boolean = false
    
    // 自动重连相关
    private var autoReconnect: Boolean = true
    private var reconnectAttempts: Int = 0
    private val maxReconnectAttempts = 10
    private val initialReconnectDelayMs = 1000L // 1秒
    private val maxReconnectDelayMs = 60000L // 1分钟
    
    // 连接状态监听器
    private var connectionListener: ConnectionListener? = null
    
    // 线程同步
    private val mutex = Mutex()
    
    companion object {
        private const val TAG = "MqttManager"
    }
    
    interface ConnectionListener {
        fun onConnected()
        fun onDisconnected()
        fun onConnectionLost(cause: Throwable?)
        fun onReconnecting(attempt: Int, delayMs: Long)
    }
    
    /**
     * 设置连接状态监听器
     */
    fun setConnectionListener(listener: ConnectionListener) {
        this.connectionListener = listener
    }
    
    /**
     * 配置MQTT连接参数
     */
    fun configure(
        serverUri: String,
        clientId: String,
        username: String? = null,
        password: String? = null,
        useTls: Boolean = false,
        autoReconnect: Boolean = true
    ) {
        this.serverUri = serverUri
        this.clientId = clientId
        this.username = username
        this.password = password
        this.useTls = useTls
        this.autoReconnect = autoReconnect
        Logger.d(TAG, "MQTT configured with server: $serverUri, clientId: $clientId")
    }
    
    /**
     * 建立MQTT连接
     */
    suspend fun connect(): Mqtt3ConnAck? {
        return mutex.withLock {
            try {
                if (serverUri.isEmpty() || clientId.isEmpty()) {
                    throw IllegalArgumentException("Server URI and client ID must be configured")
                }
                
                Logger.d(TAG, "Attempting to connect to MQTT broker: $serverUri")
                
                // 解析服务器URI
                val uri = java.net.URI(serverUri)
                val host = uri.host
                val port = if (uri.port != -1) uri.port else if (useTls) 8883 else 1883
                
                Logger.d(TAG, "Connecting to host: $host, port: $port")
                
                // 如果已存在客户端，先断开连接
                client?.toAsync()?.disconnect()?.get(5, TimeUnit.SECONDS)
                
                // 构建客户端
                val builder = Mqtt3Client.builder()
                    .serverHost(host)
                    .serverPort(port)
                    .identifier(clientId)
                    
                // 配置认证信息
                if (username != null && password != null) {
                    builder.simpleAuth(
                        Mqtt3SimpleAuth.builder()
                            .username(username!!.toByteArray())
                            .password(password!!.toByteArray())
                            .build()
                    )
                }
                
                // 配置TLS
                if (useTls) {
                    builder.sslWithDefaultConfig()
                }
                
                client = builder.build()
                
                // 连接
                val connAck = client?.toAsync()?.connect()?.get(10, TimeUnit.SECONDS)
                
                // 重置重连计数
                reconnectAttempts = 0
                
                // 通知连接成功
                connectionListener?.onConnected()
                Logger.d(TAG, "MQTT connected successfully")
                
                // 设置连接丢失回调
                client?.toAsync()?.handleDisconnect { throwable ->
                    coroutineScope.launch {
                        handleConnectionLost(throwable)
                    }
                }
                
                return connAck
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to connect to MQTT broker", e)
                connectionListener?.onConnectionLost(e)
                if (autoReconnect) {
                    scheduleReconnect()
                }
                throw e
            }
        }
    }
    
    /**
     * 处理连接丢失
     */
    private suspend fun handleConnectionLost(cause: Throwable?) {
        mutex.withLock {
            Logger.w(TAG, "MQTT connection lost", cause)
            connectionListener?.onConnectionLost(cause)
            
            // 清理客户端引用
            client = null
            
            // 通知断开连接
            connectionListener?.onDisconnected()
            
            // 如果启用了自动重连，则安排重连
            if (autoReconnect) {
                scheduleReconnect()
            }
        }
    }
    
    /**
     * 安排重连
     */
    private fun scheduleReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Logger.w(TAG, "Max reconnect attempts reached, giving up")
            // 达到最大重试次数
            return
        }
        
        reconnectAttempts++
        
        // 计算延迟时间（指数退避）
        val delayMs = min(
            initialReconnectDelayMs * (1L shl min(reconnectAttempts - 1, 6)), 
            maxReconnectDelayMs
        )
        
        Logger.d(TAG, "Scheduling reconnect attempt $reconnectAttempts in ${delayMs}ms")
        
        // 通知即将重连
        connectionListener?.onReconnecting(reconnectAttempts, delayMs)
        
        // 安排重连
        coroutineScope.launch {
            delay(delayMs)
            try {
                connect()
            } catch (e: Exception) {
                Logger.e(TAG, "Reconnect attempt $reconnectAttempts failed", e)
                // 重连失败，继续尝试
                scheduleReconnect()
            }
        }
    }
    
    /**
     * 断开MQTT连接
     */
    suspend fun disconnect() {
        mutex.withLock {
            try {
                if (client?.state?.isConnected == true) {
                    client?.toAsync()?.disconnect()?.get(5, TimeUnit.SECONDS)
                    Logger.d(TAG, "MQTT disconnected successfully")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error during MQTT disconnect", e)
                // 忽略断开连接时的异常
            } finally {
                client = null
                reconnectAttempts = 0
                connectionListener?.onDisconnected()
            }
        }
    }
    
    /**
     * 发布消息
     */
    suspend fun publish(topic: String, payload: String, qos: Int = 1, retained: Boolean = false) {
        mutex.withLock {
            client?.let { mqttClient ->
                try {
                    if (!mqttClient.state.isConnected) {
                        throw IllegalStateException("MQTT client not connected")
                    }
                    
                    Logger.d(TAG, "Publishing message to topic: $topic")
                    
                    mqttClient.toAsync().publishWith()
                        .topic(topic)
                        .payload(payload.toByteArray())
                        .qos(com.hivemq.client.mqtt.datatypes.MqttQos.fromCode(qos) ?: com.hivemq.client.mqtt.datatypes.MqttQos.AT_LEAST_ONCE)
                        .retained(retained)
                        .send()
                        .get(5, TimeUnit.SECONDS)
                        
                    Logger.d(TAG, "Message published successfully to topic: $topic")
                } catch (e: Exception) {
                    Logger.e(TAG, "Failed to publish message to topic: $topic", e)
                    // 发布失败，如果启用了自动重连则触发重连
                    if (autoReconnect) {
                        coroutineScope.launch {
                            handleConnectionLost(e)
                        }
                    }
                    throw e
                }
            } ?: throw IllegalStateException("MQTT client not connected")
        }
    }
    
    /**
     * 检查是否已连接
     */
    fun isConnected(): Boolean {
        val connected = client?.state?.isConnected == true
        Logger.d(TAG, "MQTT connection status: $connected")
        return connected
    }
    
    /**
     * 获取当前重连尝试次数
     */
    fun getReconnectAttempts(): Int {
        return reconnectAttempts
    }
}