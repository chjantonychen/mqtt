package com.example.mqttlocationtracker.privacy

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.mqttlocationtracker.utils.Logger
import java.security.MessageDigest
import kotlin.math.*

/**
 * 隐私管理器，用于保护用户位置数据的隐私
 */
class PrivacyManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val TAG = "PrivacyManager"
        private const val PREFS_NAME = "privacy_settings"
        
        // 隐私设置键名
        private const val KEY_LOCATION_ANONYMIZATION = "location_anonymization"
        private const val KEY_DATA_ENCRYPTION = "data_encryption"
        private const val KEY_AUTO_DELETE_OLD_DATA = "auto_delete_old_data"
        private const val KEY_DELETE_OLD_DATA_DAYS = "delete_old_data_days"
        private const val KEY_SHARE_ANONYMOUS_DATA = "share_anonymous_data"
        
        // 默认值
        private const val DEFAULT_LOCATION_ANONYMIZATION = false
        private const val DEFAULT_DATA_ENCRYPTION = false
        private const val DEFAULT_AUTO_DELETE_OLD_DATA = true
        private const val DEFAULT_DELETE_OLD_DATA_DAYS = 30L
        private const val DEFAULT_SHARE_ANONYMOUS_DATA = false
        
        @Volatile
        private var INSTANCE: PrivacyManager? = null
        
        fun getInstance(context: Context): PrivacyManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PrivacyManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * 获取位置匿名化设置
     */
    fun isLocationAnonymizationEnabled(): Boolean {
        return prefs.getBoolean(KEY_LOCATION_ANONYMIZATION, DEFAULT_LOCATION_ANONYMIZATION)
    }
    
    /**
     * 设置位置匿名化
     */
    fun setLocationAnonymizationEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_LOCATION_ANONYMIZATION, enabled)
        }
        Logger.d(TAG, "Location anonymization set to: $enabled")
    }
    
    /**
     * 获取数据加密设置
     */
    fun isDataEncryptionEnabled(): Boolean {
        return prefs.getBoolean(KEY_DATA_ENCRYPTION, DEFAULT_DATA_ENCRYPTION)
    }
    
    /**
     * 设置数据加密
     */
    fun setDataEncryptionEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_DATA_ENCRYPTION, enabled)
        }
        Logger.d(TAG, "Data encryption set to: $enabled")
    }
    
    /**
     * 获取自动删除旧数据设置
     */
    fun isAutoDeleteOldDataEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_DELETE_OLD_DATA, DEFAULT_AUTO_DELETE_OLD_DATA)
    }
    
    /**
     * 设置自动删除旧数据
     */
    fun setAutoDeleteOldDataEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_AUTO_DELETE_OLD_DATA, enabled)
        }
        Logger.d(TAG, "Auto delete old data set to: $enabled")
    }
    
    /**
     * 获取删除旧数据的天数
     */
    fun getDeleteOldDataDays(): Long {
        return prefs.getLong(KEY_DELETE_OLD_DATA_DAYS, DEFAULT_DELETE_OLD_DATA_DAYS)
    }
    
    /**
     * 设置删除旧数据的天数
     */
    fun setDeleteOldDataDays(days: Long) {
        prefs.edit {
            putLong(KEY_DELETE_OLD_DATA_DAYS, days)
        }
        Logger.d(TAG, "Delete old data days set to: $days")
    }
    
    /**
     * 获取分享匿名数据设置
     */
    fun isShareAnonymousDataEnabled(): Boolean {
        return prefs.getBoolean(KEY_SHARE_ANONYMOUS_DATA, DEFAULT_SHARE_ANONYMOUS_DATA)
    }
    
    /**
     * 设置分享匿名数据
     */
    fun setShareAnonymousDataEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_SHARE_ANONYMOUS_DATA, enabled)
        }
        Logger.d(TAG, "Share anonymous data set to: $enabled")
    }
    
    /**
     * 匿名化位置数据
     */
    fun anonymizeLocation(latitude: Double, longitude: Double, precision: Int = 3): Pair<Double, Double> {
        if (!isLocationAnonymizationEnabled()) {
            return Pair(latitude, longitude)
        }
        
        try {
            // 将坐标精度降低到指定小数位数
            val factor = 10.0.pow(precision)
            val anonLat = round(latitude * factor) / factor
            val anonLng = round(longitude * factor) / factor
            
            Logger.d(TAG, "Location anonymized from ($latitude, $longitude) to ($anonLat, $anonLng)")
            return Pair(anonLat, anonLng)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to anonymize location", e)
            return Pair(latitude, longitude)
        }
    }
    
    /**
     * 加密数据
     */
    fun encryptData(data: String): String {
        if (!isDataEncryptionEnabled()) {
            return data
        }
        
        return try {
            // 简单的SHA-256哈希加密（实际应用中应使用更强的加密算法）
            val md = MessageDigest.getInstance("SHA-256")
            val hashBytes = md.digest(data.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to encrypt data", e)
            data
        }
    }
    
    /**
     * 获取所有隐私设置
     */
    fun getAllPrivacySettings(): PrivacySettings {
        return PrivacySettings(
            locationAnonymization = isLocationAnonymizationEnabled(),
            dataEncryption = isDataEncryptionEnabled(),
            autoDeleteOldData = isAutoDeleteOldDataEnabled(),
            deleteOldDataDays = getDeleteOldDataDays(),
            shareAnonymousData = isShareAnonymousDataEnabled()
        )
    }
    
    /**
     * 隐私设置数据类
     */
    data class PrivacySettings(
        val locationAnonymization: Boolean,
        val dataEncryption: Boolean,
        val autoDeleteOldData: Boolean,
        val deleteOldDataDays: Long,
        val shareAnonymousData: Boolean
    )
}