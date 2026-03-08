package com.example.mqttlocationtracker.data

import org.junit.Test
import org.junit.Assert.*
import org.json.JSONObject

class LocationDataTest {

    @Test
    fun testToJson() {
        // 创建一个LocationData实例
        val locationData = LocationData(
            latitude = 39.9042,
            longitude = 116.4074,
            timestamp = 1640995200000L, // 2022-01-01 00:00:00 UTC
            accuracy = 10.5f,
            altitude = 42.0,
            speed = 2.3f,
            bearing = 45.0f
        )

        // 转换为JSON
        val jsonString = locationData.toJson()
        
        // 解析JSON并验证
        val jsonObject = JSONObject(jsonString)
        
        assertEquals(39.9042, jsonObject.getDouble("latitude"), 0.0001)
        assertEquals(116.4074, jsonObject.getDouble("longitude"), 0.0001)
        assertEquals(1640995200000L, jsonObject.getLong("timestamp"))
        assertEquals(10.5, jsonObject.getDouble("accuracy"), 0.001)
        assertEquals(42.0, jsonObject.getDouble("altitude"), 0.001)
        assertEquals(2.3, jsonObject.getDouble("speed"), 0.001)
        assertEquals(45.0, jsonObject.getDouble("bearing"), 0.001)
        assertTrue(jsonObject.has("time"))
    }

    @Test
    fun testToJsonWithoutOptionalFields() {
        // 创建一个只包含必需字段的LocationData实例
        val locationData = LocationData(
            latitude = 39.9042,
            longitude = 116.4074,
            timestamp = 1640995200000L
        )

        // 转换为JSON
        val jsonString = locationData.toJson()
        
        // 解析JSON并验证
        val jsonObject = JSONObject(jsonString)
        
        assertEquals(39.9042, jsonObject.getDouble("latitude"), 0.0001)
        assertEquals(116.4074, jsonObject.getDouble("longitude"), 0.0001)
        assertEquals(1640995200000L, jsonObject.getLong("timestamp"))
        assertFalse(jsonObject.has("accuracy"))
        assertFalse(jsonObject.has("altitude"))
        assertFalse(jsonObject.has("speed"))
        assertFalse(jsonObject.has("bearing"))
        assertTrue(jsonObject.has("time"))
    }

    @Test
    fun testFromJson() {
        // 创建JSON字符串
        val jsonString = """{
            "latitude": 39.9042,
            "longitude": 116.4074,
            "timestamp": 1640995200000,
            "accuracy": 10.5,
            "altitude": 42.0,
            "speed": 2.3,
            "bearing": 45.0,
            "time": "2022-01-01 00:00:00"
        }"""

        // 从JSON创建LocationData
        val locationData = LocationData.fromJson(jsonString)
        
        // 验证属性
        assertEquals(39.9042, locationData.latitude, 0.0001)
        assertEquals(116.4074, locationData.longitude, 0.0001)
        assertEquals(1640995200000L, locationData.timestamp)
        assertEquals(10.5f, locationData.accuracy!!, 0.001f)
        assertEquals(42.0, locationData.altitude!!, 0.001)
        assertEquals(2.3f, locationData.speed!!, 0.001f)
        assertEquals(45.0f, locationData.bearing!!, 0.001f)
    }
}