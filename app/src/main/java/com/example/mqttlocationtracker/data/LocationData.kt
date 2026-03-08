package com.example.mqttlocationtracker.data

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
    
    /**
     * 将位置数据转换为JSON格式
     */
    fun toJson(): String {
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
        
        return json.toString()
    }
    
    /**
     * 从JSON字符串创建LocationData对象
     */
    companion object {
        fun fromJson(jsonString: String): LocationData {
            val json = JSONObject(jsonString)
            return LocationData(
                latitude = json.getDouble("latitude"),
                longitude = json.getDouble("longitude"),
                timestamp = json.getLong("timestamp"),
                accuracy = if (json.has("accuracy")) json.getDouble("accuracy").toFloat() else null,
                altitude = if (json.has("altitude")) json.getDouble("altitude") else null,
                speed = if (json.has("speed")) json.getDouble("speed").toFloat() else null,
                bearing = if (json.has("bearing")) json.getDouble("bearing").toFloat() else null
            )
        }
    }
}