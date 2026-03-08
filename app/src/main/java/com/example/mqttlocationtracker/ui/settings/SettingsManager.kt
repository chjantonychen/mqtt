package com.example.mqttlocationtracker.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * 设置数据模型类，用于管理应用设置
 */
class SettingsManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "mqtt_location_tracker_prefs"
        
        // MQTT设置键名
        private const val KEY_MQTT_SERVER_URI = "mqtt_server_uri"
        private const val KEY_MQTT_CLIENT_ID = "mqtt_client_id"
        private const val KEY_MQTT_USERNAME = "mqtt_username"
        private const val KEY_MQTT_PASSWORD = "mqtt_password"
        private const val KEY_MQTT_USE_TLS = "mqtt_use_tls"
        private const val KEY_MQTT_TOPIC = "mqtt_topic"
        
        // 位置跟踪设置键名
        private const val KEY_LOCATION_UPDATE_INTERVAL = "location_update_interval"
        private const val KEY_LOCATION_MIN_DISTANCE = "location_min_distance"
        
        // 数据清理设置键名
        private const val KEY_DATA_RETENTION_DAYS = "data_retention_days"
        private const val DEFAULT_DATA_RETENTION_DAYS = 30L
        
        // 日志设置键名
        private const val KEY_LOGGING_ENABLED = "logging_enabled"
        private const val DEFAULT_LOGGING_ENABLED = true
        
        // 默认值
        private const val DEFAULT_MQTT_SERVER_URI = "tcp://broker.hivemq.com:1883"
        private const val DEFAULT_MQTT_CLIENT_ID = "android_client"
        private const val DEFAULT_MQTT_TOPIC = "location/tracker"
        private const val DEFAULT_LOCATION_UPDATE_INTERVAL = 1000L // 1秒
        private const val DEFAULT_LOCATION_MIN_DISTANCE = 0f // 0米
        
        @Volatile
        private var INSTANCE: SettingsManager? = null
        
        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * 获取MQTT服务器URI
     */
    fun getMqttServerUri(): String {
        return prefs.getString(KEY_MQTT_SERVER_URI, DEFAULT_MQTT_SERVER_URI) ?: DEFAULT_MQTT_SERVER_URI
    }
    
    /**
     * 设置MQTT服务器URI
     */
    fun setMqttServerUri(uri: String) {
        prefs.edit {
            putString(KEY_MQTT_SERVER_URI, uri)
        }
    }
    
    /**
     * 获取MQTT客户端ID
     */
    fun getMqttClientId(): String {
        return prefs.getString(KEY_MQTT_CLIENT_ID, DEFAULT_MQTT_CLIENT_ID) ?: DEFAULT_MQTT_CLIENT_ID
    }
    
    /**
     * 设置MQTT客户端ID
     */
    fun setMqttClientId(clientId: String) {
        prefs.edit {
            putString(KEY_MQTT_CLIENT_ID, clientId)
        }
    }
    
    /**
     * 获取MQTT用户名
     */
    fun getMqttUsername(): String? {
        return prefs.getString(KEY_MQTT_USERNAME, null)
    }
    
    /**
     * 设置MQTT用户名
     */
    fun setMqttUsername(username: String?) {
        prefs.edit {
            putString(KEY_MQTT_USERNAME, username)
        }
    }
    
    /**
     * 获取MQTT密码
     */
    fun getMqttPassword(): String? {
        return prefs.getString(KEY_MQTT_PASSWORD, null)
    }
    
    /**
     * 设置MQTT密码
     */
    fun setMqttPassword(password: String?) {
        prefs.edit {
            putString(KEY_MQTT_PASSWORD, password)
        }
    }
    
    /**
     * 获取是否使用TLS
     */
    fun isMqttUseTls(): Boolean {
        return prefs.getBoolean(KEY_MQTT_USE_TLS, false)
    }
    
    /**
     * 设置是否使用TLS
     */
    fun setMqttUseTls(useTls: Boolean) {
        prefs.edit {
            putBoolean(KEY_MQTT_USE_TLS, useTls)
        }
    }
    
    /**
     * 获取MQTT主题
     */
    fun getMqttTopic(): String {
        return prefs.getString(KEY_MQTT_TOPIC, DEFAULT_MQTT_TOPIC) ?: DEFAULT_MQTT_TOPIC
    }
    
    /**
     * 设置MQTT主题
     */
    fun setMqttTopic(topic: String) {
        prefs.edit {
            putString(KEY_MQTT_TOPIC, topic)
        }
    }
    
    /**
     * 获取位置更新间隔（毫秒）
     */
    fun getLocationUpdateInterval(): Long {
        return prefs.getLong(KEY_LOCATION_UPDATE_INTERVAL, DEFAULT_LOCATION_UPDATE_INTERVAL)
    }
    
    /**
     * 设置位置更新间隔（毫秒）
     */
    fun setLocationUpdateInterval(interval: Long) {
        prefs.edit {
            putLong(KEY_LOCATION_UPDATE_INTERVAL, interval)
        }
    }
    
    /**
     * 获取位置最小更新距离（米）
     */
    fun getLocationMinDistance(): Float {
        return prefs.getFloat(KEY_LOCATION_MIN_DISTANCE, DEFAULT_LOCATION_MIN_DISTANCE)
    }
    
    /**
     * 设置位置最小更新距离（米）
     */
    fun setLocationMinDistance(distance: Float) {
        prefs.edit {
            putFloat(KEY_LOCATION_MIN_DISTANCE, distance)
        }
    }
    
    /**
     * 获取数据保留天数
     */
    fun getDataRetentionDays(): Long {
        return prefs.getLong(KEY_DATA_RETENTION_DAYS, DEFAULT_DATA_RETENTION_DAYS)
    }
    
    /**
     * 设置数据保留天数
     */
    fun setDataRetentionDays(days: Long) {
        prefs.edit {
            putLong(KEY_DATA_RETENTION_DAYS, days)
        }
    }
    
    /**
     * 获取是否启用日志记录
     */
    fun isLoggingEnabled(): Boolean {
        return prefs.getBoolean(KEY_LOGGING_ENABLED, DEFAULT_LOGGING_ENABLED)
    }
    
    /**
     * 设置是否启用日志记录
     */
    fun setLoggingEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_LOGGING_ENABLED, enabled)
        }
    }
    
    /**
     * 清除所有设置
     */
    fun clearAll() {
        prefs.edit {
            clear()
        }
    }
}