package com.example.mqttlocationtracker.utils

import android.content.Context
import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * GeoJSON生成器，用于将位置数据导出为GeoJSON格式
 */
object GeoJsonGenerator {
    
    private const val TAG = "GeoJsonGenerator"
    
    /**
     * 生成位置数据GeoJSON文件
     */
    fun generateLocationDataGeoJson(
        context: Context,
        locations: List<LocationEntity>,
        outputStream: FileOutputStream
    ): Boolean {
        return try {
            val geoJsonContent = buildGeoJsonData(context, locations)
            outputStream.write(geoJsonContent.toByteArray())
            outputStream.flush()
            outputStream.close()
            
            Logger.i(TAG, "GeoJSON data generated successfully with ${locations.size} locations")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate GeoJSON data", e)
            false
        }
    }
    
    /**
     * 构建GeoJSON数据
     */
    private fun buildGeoJsonData(context: Context, locations: List<LocationEntity>): String {
        val geoJson = StringBuilder()
        
        // GeoJSON头部
        geoJson.append("{\n")
        geoJson.append("  \"type\": \"FeatureCollection\",\n")
        geoJson.append("  \"name\": \"MQTT位置跟踪数据\",\n")
        geoJson.append("  \"crs\": {\n")
        geoJson.append("    \"type\": \"name\",\n")
        geoJson.append("    \"properties\": {\n")
        geoJson.append("      \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\"\n")
        geoJson.append("    }\n")
        geoJson.append("  },\n")
        geoJson.append("  \"features\": [\n")
        
        // 添加位置特征
        for ((index, location) in locations.withIndex()) {
            geoJson.append("    {\n")
            geoJson.append("      \"type\": \"Feature\",\n")
            geoJson.append("      \"properties\": {\n")
            geoJson.append("        \"id\": ${location.id},\n")
            geoJson.append("        \"timestamp\": ${location.timestamp},\n")
            geoJson.append("        \"formatted_time\": \"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(location.timestamp))}\",\n")
            
            location.accuracy?.let { 
                geoJson.append("        \"accuracy\": $it,\n") 
            }
            
            location.altitude?.let { 
                geoJson.append("        \"altitude\": $it,\n") 
            }
            
            location.speed?.let { 
                geoJson.append("        \"speed\": $it,\n") 
            }
            
            location.bearing?.let { 
                geoJson.append("        \"bearing\": $it,\n") 
            }
            
            geoJson.append("        \"synced\": ${location.isSynced},\n")
            geoJson.append("        \"created_at\": ${location.createdAt}\n")
            geoJson.append("      },\n")
            geoJson.append("      \"geometry\": {\n")
            geoJson.append("        \"type\": \"Point\",\n")
            geoJson.append("        \"coordinates\": [\n")
            geoJson.append("          ${location.longitude},\n")
            geoJson.append("          ${location.latitude}")
            
            // 如果有海拔信息，添加第三个坐标
            location.altitude?.let { 
                geoJson.append(",\n          $it")
            }
            
            geoJson.append("\n        ]\n")
            geoJson.append("      }\n")
            geoJson.append("    }")
            
            // 添加逗号（除了最后一个元素）
            if (index < locations.size - 1) {
                geoJson.append(",")
            }
            
            geoJson.append("\n")
        }
        
        // 结束特征数组和GeoJSON对象
        geoJson.append("  ]\n")
        geoJson.append("}")
        
        return geoJson.toString()
    }
    
    /**
     * 生成GeoJSON轨迹线
     */
    fun generateLocationTrackGeoJson(
        context: Context,
        locations: List<LocationEntity>,
        outputStream: FileOutputStream
    ): Boolean {
        return try {
            val geoJsonContent = buildGeoJsonTrack(context, locations)
            outputStream.write(geoJsonContent.toByteArray())
            outputStream.flush()
            outputStream.close()
            
            Logger.i(TAG, "GeoJSON track generated successfully with ${locations.size} locations")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate GeoJSON track", e)
            false
        }
    }
    
    /**
     * 构建GeoJSON轨迹线数据
     */
    private fun buildGeoJsonTrack(context: Context, locations: List<LocationEntity>): String {
        val geoJson = StringBuilder()
        
        // GeoJSON头部
        geoJson.append("{\n")
        geoJson.append("  \"type\": \"FeatureCollection\",\n")
        geoJson.append("  \"name\": \"MQTT位置轨迹\",\n")
        geoJson.append("  \"crs\": {\n")
        geoJson.append("    \"type\": \"name\",\n")
        geoJson.append("    \"properties\": {\n")
        geoJson.append("      \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\"\n")
        geoJson.append("    }\n")
        geoJson.append("  },\n")
        geoJson.append("  \"features\": [\n")
        
        // 添加轨迹线特征
        if (locations.isNotEmpty()) {
            geoJson.append("    {\n")
            geoJson.append("      \"type\": \"Feature\",\n")
            geoJson.append("      \"properties\": {\n")
            geoJson.append("        \"name\": \"位置轨迹\",\n")
            geoJson.append("        \"total_points\": ${locations.size},\n")
            geoJson.append("        \"start_time\": \"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(locations.first().timestamp))}\",\n")
            geoJson.append("        \"end_time\": \"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(locations.last().timestamp))}\"\n")
            geoJson.append("      },\n")
            geoJson.append("      \"geometry\": {\n")
            geoJson.append("        \"type\": \"LineString\",\n")
            geoJson.append("        \"coordinates\": [\n")
            
            // 添加轨迹点坐标
            for ((index, location) in locations.withIndex()) {
                geoJson.append("          [\n")
                geoJson.append("            ${location.longitude},\n")
                geoJson.append("            ${location.latitude}")
                
                // 如果有海拔信息，添加第三个坐标
                location.altitude?.let { 
                    geoJson.append(",\n            $it")
                }
                
                geoJson.append("\n          ]")
                
                // 添加逗号（除了最后一个元素）
                if (index < locations.size - 1) {
                    geoJson.append(",")
                }
                
                geoJson.append("\n")
            }
            
            geoJson.append("        ]\n")
            geoJson.append("      }\n")
            geoJson.append("    }")
        }
        
        // 结束特征数组和GeoJSON对象
        geoJson.append("\n  ]\n")
        geoJson.append("}")
        
        return geoJson.toString()
    }
    
    /**
     * 生成GeoJSON示例
     */
    fun generateGeoJsonExample(): String {
        return """
{
  "type": "FeatureCollection",
  "name": "MQTT位置跟踪数据示例",
  "crs": {
    "type": "name",
    "properties": {
      "name": "urn:ogc:def:crs:OGC:1.3:CRS84"
    }
  },
  "features": [
    {
      "type": "Feature",
      "properties": {
        "id": 1,
        "timestamp": 1672531200000,
        "formatted_time": "2023-01-01 00:00:00",
        "accuracy": 5.0,
        "altitude": 43.5,
        "speed": 0.0,
        "bearing": 0.0,
        "synced": true,
        "created_at": 1672531200000
      },
      "geometry": {
        "type": "Point",
        "coordinates": [
          116.4074,
          39.9042,
          43.5
        ]
      }
    }
  ]
}
        """.trimIndent()
    }
}