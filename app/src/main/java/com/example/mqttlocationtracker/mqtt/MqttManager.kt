package com.example.mqttlocationtracker.mqtt

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.net.ssl.SSLSocketFactory
import kotlin.coroutines.CoroutineContext

/**
 * MQTT客户端管理类
 */
class MqttManager(private val coroutineScope: CoroutineScope) {
    
    private var client: Mqtt3Client? = null
    private var serverUri: String = ""
    private var clientId: String = ""
    private var username: String? = null
    private var password: String? = null
    private var useTls: Boolean = false
    
    /**
     * 配置MQTT连接参数
     */
    fun configure(
        serverUri: String,
        clientId: String,
        username: String? = null,
        password: String? = null,
        useTls: Boolean = false
    ) {
        this.serverUri = serverUri
        this.clientId = clientId
        this.username = username
        this.password = password
        this.useTls = useTls
    }
    
    /**
     * 建立MQTT连接
     */
    suspend fun connect(): Mqtt3ConnAck? {
        if (serverUri.isEmpty() || clientId.isEmpty()) {
            throw IllegalArgumentException("Server URI and client ID must be configured")
        }
        
        // 解析服务器URI
        val uri = java.net.URI(serverUri)
        val host = uri.host
        val port = if (uri.port != -1) uri.port else if (useTls) 8883 else 1883
        
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
            builder.sslConfig()
                .apply {
                    // 使用默认的SSL配置
                }
        }
        
        client = builder.build()
        
        // 连接
        return client?.toAsync()?.connect()?.get()
    }
    
    /**
     * 断开MQTT连接
     */
    suspend fun disconnect() {
        client?.toAsync()?.disconnect()?.get()
        client = null
    }
    
    /**
     * 发布消息
     */
    suspend fun publish(topic: String, payload: String, qos: Int = 1, retained: Boolean = false) {
        client?.let { mqttClient ->
            mqttClient.toAsync().publishWith()
                .topic(topic)
                .payload(payload.toByteArray())
                .qos(com.hivemq.client.mqtt.datatypes.MqttQos.fromCode(qos) ?: com.hivemq.client.mqtt.datatypes.MqttQos.AT_LEAST_ONCE)
                .retained(retained)
                .send()
                .get()
        }
    }
    
    /**
     * 检查是否已连接
     */
    fun isConnected(): Boolean {
        return client?.state?.isConnected == true
    }
}