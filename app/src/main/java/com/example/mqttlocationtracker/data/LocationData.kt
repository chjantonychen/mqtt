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
     * 将位置数据转换为CSV格式
     */
    fun toCsv(): String {
        return try {
            // CSV格式: latitude,longitude,timestamp,accuracy,altitude,speed,bearing,time
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val timeString = sdf.format(Date(timestamp))
            
            val csvRow = listOf(
                latitude.toString(),
                longitude.toString(),
                timestamp.toString(),
                accuracy?.toString() ?: "",
                altitude?.toString() ?: "",
                speed?.toString() ?: "",
                bearing?.toString() ?: "",
                timeString
            ).joinToString(",")
            
            Logger.d(TAG, "Location data converted to CSV: $csvRow")
            csvRow
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to convert location data to CSV", e)
            throw e
        }
    }
    
    /**
     * 获取CSV头部
     */
    fun getCsvHeader(): String {
        return "latitude,longitude,timestamp,accuracy,altitude,speed,bearing,time"
    }
    
    /**
     * 将位置数据转换为KML格式的Placemark
     */
    fun toKmlPlacemark(index: Int = 0): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val timeString = sdf.format(Date(timestamp))
            
            """
            <Placemark>
                <name>位置点 #$index</name>
                <TimeStamp>
                    <when>$timeString</when>
                </TimeStamp>
                <Point>
                    <coordinates>$longitude,$latitude${if (altitude != null) ",$altitude" else ""}</coordinates>
                </Point>
                <description>
                    <![CDATA[
                        <b>位置信息</b><br/>
                        纬度: $latitude<br/>
                        经度: $longitude<br/>
                        ${if (altitude != null) "海拔: $altitude 米<br/>" else ""}
                        ${if (accuracy != null) "精度: $accuracy 米<br/>" else ""}
                        ${if (speed != null) "速度: $speed m/s<br/>" else ""}
                        时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))}
                    ]]>
                </description>
            </Placemark>
            """.trimIndent()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to convert location data to KML Placemark", e)
            throw e
        }
    }
    
    /**
     * 生成KML文件头部
     */
    fun getKmlHeader(): String {
        return """<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>
    <name>MQTT位置跟踪数据</name>
    <description>由MQTT位置跟踪器导出的位置数据</description>
"""
    }
    
    /**
     * 生成KML文件尾部
     */
    fun getKmlFooter(): String {
        return """</Document>
</kml>"""
    }
    
    /**
     * 将位置数据转换为GPX格式的TrackPoint
     */
    fun toGpxTrackPoint(): String {
        return try {
            val timeString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date(timestamp))
            
            val trackPoint = StringBuilder()
            trackPoint.append("      <trkpt lat=\"$latitude\" lon=\"$longitude\">\n")
            
            altitude?.let {
                trackPoint.append("        <ele>$it</ele>\n")
            }
            
            trackPoint.append("        <time>$timeString</time>\n")
            
            accuracy?.let {
                trackPoint.append("        <hdop>$it</hdop>\n")
            }
            
            speed?.let {
                trackPoint.append("        <speed>$it</speed>\n")
            }
            
            trackPoint.append("      </trkpt>\n")
            
            trackPoint.toString()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to convert location data to GPX TrackPoint", e)
            throw e
        }
    }
    
    /**
     * 生成GPX文件头部
     */
    fun getGpxHeader(): String {
        return """<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="MQTT位置跟踪器" xmlns="http://www.topografix.com/GPX/1/1">
  <trk>
    <name>MQTT位置跟踪数据</name>
    <desc>由MQTT位置跟踪器导出的位置数据</desc>
    <trkseg>
"""
    }
    
    /**
     * 生成GPX文件尾部
     */
    fun getGpxFooter(): String {
        return """    </trkseg>
  </trk>
</gpx>"""
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