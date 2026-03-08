package com.example.mqttlocationtracker.data

import com.example.mqttlocationtracker.utils.Logger
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * 位置数据模型类
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float? = null,
    val altitude: Double? = null,
    val speed: Float? = null,
    val bearing: Float? = null
) {
    
    companion object {
        private const val TAG = "LocationData"
    }
    
    /**
     * 将位置数据转换为JSON格式
     */
    fun toJson(): String {
        return try {
            val json = JSONObject()
            json.put("latitude", latitude)
            json.put("longitude", longitude)
            json.put("timestamp", timestamp)
            
            accuracy?.let { json.put("accuracy", it) }
            altitude?.let { json.put("altitude", it) }
            speed?.let { json.put("speed", it) }
            bearing?.let { json.put("bearing", it) }
            
            // 添加格式化的时间字符串
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            json.put("time", sdf.format(Date(timestamp)))
            
            val jsonString = json.toString()
            Logger.d(TAG, "Location data converted to JSON: $jsonString")
            jsonString
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to convert location data to JSON", e)
            throw e
        }
    }
    
    /**
     * 从JSON字符串创建LocationData对象
     */
    companion object {
        fun fromJson(jsonString: String): LocationData {
            return try {
                val json = JSONObject(jsonString)
                val locationData = LocationData(
                    latitude = json.getDouble("latitude"),
                    longitude = json.getDouble("longitude"),
                    timestamp = json.getLong("timestamp"),
                    accuracy = if (json.has("accuracy")) json.getDouble("accuracy").toFloat() else null,
                    altitude = if (json.has("altitude")) json.getDouble("altitude") else null,
                    speed = if (json.has("speed")) json.getDouble("speed").toFloat() else null,
                    bearing = if (json.has("bearing")) json.getDouble("bearing").toFloat() else null
                )
                
                Logger.d(TAG, "Location data created from JSON: $jsonString")
                locationData
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to create location data from JSON", e)
                throw e
            }
        }
    }
}