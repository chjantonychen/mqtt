package com.example.mqttlocationtracker.cloud

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.example.mqttlocationtracker.database.repository.LocationRepository
import com.example.mqttlocationtracker.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8

/**
 * 云同步管理器，用于将位置数据同步到云端
 */
class CloudSyncManager private constructor(
    private val context: Context,
    private val locationRepository: LocationRepository,
    private val coroutineScope: CoroutineScope
) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val TAG = "CloudSyncManager"
        private const val PREFS_NAME = "cloud_sync_settings"
        
        // 云同步设置键名
        private const val KEY_CLOUD_SYNC_ENABLED = "cloud_sync_enabled"
        private const val KEY_CLOUD_ENDPOINT = "cloud_endpoint"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_SYNC_INTERVAL = "sync_interval"
        private const val KEY_ENCRYPTION_KEY = "encryption_key"
        
        // 默认值
        private const val DEFAULT_CLOUD_SYNC_ENABLED = false
        private const val DEFAULT_CLOUD_ENDPOINT = "https://api.example.com/locations"
        private const val DEFAULT_SYNC_INTERVAL = 300000L // 5分钟
        private const val DEFAULT_ENCRYPTION_KEY = "default_encryption_key_32_bytes_long!" // 32字节密钥
        
        @Volatile
        private var INSTANCE: CloudSyncManager? = null
        
        fun getInstance(
            context: Context,
            locationRepository: LocationRepository,
            coroutineScope: CoroutineScope
        ): CloudSyncManager {
            return INSTANCE ?: synchronized(this) {
                val instance = CloudSyncManager(
                    context.applicationContext,
                    locationRepository,
                    coroutineScope
                )
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * 获取云同步是否启用
     */
    fun isCloudSyncEnabled(): Boolean {
        return prefs.getBoolean(KEY_CLOUD_SYNC_ENABLED, DEFAULT_CLOUD_SYNC_ENABLED)
    }
    
    /**
     * 设置云同步启用状态
     */
    fun setCloudSyncEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_CLOUD_SYNC_ENABLED, enabled)
        }
        Logger.d(TAG, "Cloud sync enabled set to: $enabled")
    }
    
    /**
     * 获取云同步端点
     */
    fun getCloudEndpoint(): String {
        return prefs.getString(KEY_CLOUD_ENDPOINT, DEFAULT_CLOUD_ENDPOINT) ?: DEFAULT_CLOUD_ENDPOINT
    }
    
    /**
     * 设置云同步端点
     */
    fun setCloudEndpoint(endpoint: String) {
        prefs.edit {
            putString(KEY_CLOUD_ENDPOINT, endpoint)
        }
        Logger.d(TAG, "Cloud endpoint set to: $endpoint")
    }
    
    /**
     * 获取API密钥
     */
    fun getApiKey(): String {
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }
    
    /**
     * 设置API密钥
     */
    fun setApiKey(apiKey: String) {
        prefs.edit {
            putString(KEY_API_KEY, apiKey)
        }
        Logger.d(TAG, "API key updated")
    }
    
    /**
     * 获取上次同步时间
     */
    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0)
    }
    
    /**
     * 设置上次同步时间
     */
    fun setLastSyncTime(time: Long) {
        prefs.edit {
            putLong(KEY_LAST_SYNC_TIME, time)
        }
        Logger.d(TAG, "Last sync time set to: $time")
    }
    
    /**
     * 获取同步间隔
     */
    fun getSyncInterval(): Long {
        return prefs.getLong(KEY_SYNC_INTERVAL, DEFAULT_SYNC_INTERVAL)
    }
    
    /**
     * 设置同步间隔
     */
    fun setSyncInterval(interval: Long) {
        prefs.edit {
            putLong(KEY_SYNC_INTERVAL, interval)
        }
        Logger.d(TAG, "Sync interval set to: $interval ms")
    }
    
    /**
     * 获取加密密钥
     */
    fun getEncryptionKey(): String {
        return prefs.getString(KEY_ENCRYPTION_KEY, DEFAULT_ENCRYPTION_KEY) ?: DEFAULT_ENCRYPTION_KEY
    }
    
    /**
     * 设置加密密钥
     */
    fun setEncryptionKey(key: String) {
        prefs.edit {
            putString(KEY_ENCRYPTION_KEY, key)
        }
        Logger.d(TAG, "Encryption key updated")
    }
    
    /**
     * 同步位置数据到云端
     */
    fun syncLocationsToCloud(onComplete: (Boolean, String) -> Unit) {
        if (!isCloudSyncEnabled()) {
            Logger.d(TAG, "Cloud sync is disabled")
            onComplete(false, "云同步未启用")
            return
        }
        
        coroutineScope.launch(Dispatchers.IO) {
            try {
                Logger.d(TAG, "Starting cloud sync")
                
                // 获取未同步的位置数据
                val unsyncedLocations = locationRepository.getUnsyncedLocations(100) // 限制每次同步100条
                
                if (unsyncedLocations.isEmpty()) {
                    Logger.d(TAG, "No unsynced locations to sync")
                    withContext(Dispatchers.Main) {
                        onComplete(true, "没有需要同步的数据")
                    }
                    return@launch
                }
                
                Logger.d(TAG, "Syncing ${unsyncedLocations.size} locations to cloud")
                
                // 准备同步数据
                val syncData = prepareSyncData(unsyncedLocations)
                
                // 发送到云端
                val result = sendToCloud(syncData)
                
                if (result.success) {
                    // 标记已同步的数据
                    val ids = unsyncedLocations.map { it.id }
                    locationRepository.markAsSynced(ids)
                    setLastSyncTime(System.currentTimeMillis())
                    
                    Logger.i(TAG, "Successfully synced ${unsyncedLocations.size} locations to cloud")
                    withContext(Dispatchers.Main) {
                        onComplete(true, "成功同步 ${unsyncedLocations.size} 条位置数据")
                    }
                } else {
                    Logger.e(TAG, "Failed to sync locations to cloud: ${result.errorMessage}")
                    withContext(Dispatchers.Main) {
                        onComplete(false, "同步失败: ${result.errorMessage}")
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error during cloud sync", e)
                withContext(Dispatchers.Main) {
                    onComplete(false, "同步出错: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 准备同步数据
     */
    private fun prepareSyncData(locations: List<LocationEntity>): String {
        return try {
            val jsonData = buildString {
                append("[")
                locations.forEachIndexed { index, location ->
                    if (index > 0) append(",")
                    append("{")
                    append("\"id\":${location.id},")
                    append("\"latitude\":${location.latitude},")
                    append("\"longitude\":${location.longitude},")
                    append("\"timestamp\":${location.timestamp}")
                    location.accuracy?.let { append(",\"accuracy\":$it") }
                    location.altitude?.let { append(",\"altitude\":$it") }
                    location.speed?.let { append(",\"speed\":$it") }
                    append("}")
                }
                append("]")
            }
            
            // 如果启用了加密，则加密数据
            if (getEncryptionKey().isNotEmpty()) {
                encryptData(jsonData)
            } else {
                jsonData
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to prepare sync data", e)
            "[]"
        }
    }
    
    /**
     * 发送数据到云端
     */
    private fun sendToCloud(data: String): SyncResult {
        return try {
            val url = URL(getCloudEndpoint())
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer ${getApiKey()}")
                doOutput = true
            }
            
            // 发送数据
            connection.outputStream.use { outputStream ->
                outputStream.write(data.toByteArray(UTF_8))
            }
            
            // 检查响应
            val responseCode = connection.responseCode
            val responseMessage = connection.responseMessage
            
            connection.disconnect()
            
            if (responseCode in 200..299) {
                SyncResult(success = true, errorMessage = null)
            } else {
                SyncResult(success = false, errorMessage = "HTTP $responseCode: $responseMessage")
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to send data to cloud", e)
            SyncResult(success = false, errorMessage = e.message)
        }
    }
    
    /**
     * 加密数据
     */
    private fun encryptData(data: String): String {
        return try {
            val key = getEncryptionKey().take(32).toByteArray(UTF_8) // 确保密钥长度为32字节
            val secretKey = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedBytes = cipher.doFinal(data.toByteArray(UTF_8))
            android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to encrypt data", e)
            data // 加密失败时返回原始数据
        }
    }
    
    /**
     * 检查是否需要同步
     */
    fun shouldSync(): Boolean {
        if (!isCloudSyncEnabled()) {
            return false
        }
        
        val lastSyncTime = getLastSyncTime()
        val currentTime = System.currentTimeMillis()
        val syncInterval = getSyncInterval()
        
        return (currentTime - lastSyncTime) >= syncInterval
    }
    
    /**
     * 获取云同步状态
     */
    fun getCloudSyncStatus(): CloudSyncStatus {
        return CloudSyncStatus(
            enabled = isCloudSyncEnabled(),
            endpoint = getCloudEndpoint(),
            lastSyncTime = getLastSyncTime(),
            syncInterval = getSyncInterval(),
            isApiKeySet = getApiKey().isNotEmpty()
        )
    }
    
    /**
     * 同步结果数据类
     */
    data class SyncResult(
        val success: Boolean,
        val errorMessage: String?
    )
    
    /**
     * 云同步状态数据类
     */
    data class CloudSyncStatus(
        val enabled: Boolean,
        val endpoint: String,
        val lastSyncTime: Long,
        val syncInterval: Long,
        val isApiKeySet: Boolean
    ) {
        /**
         * 格式化上次同步时间
         */
        fun formatLastSyncTime(): String {
            return if (lastSyncTime > 0) {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(lastSyncTime))
            } else {
                "从未同步"
            }
        }
        
        /**
         * 获取下次同步时间
         */
        fun getNextSyncTime(): Long {
            return lastSyncTime + syncInterval
        }
    }
}