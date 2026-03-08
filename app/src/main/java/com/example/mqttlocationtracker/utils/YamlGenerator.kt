package com.example.mqttlocationtracker.utils

import android.content.Context
import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * YAML生成器，用于将位置数据导出为YAML格式
 */
object YamlGenerator {
    
    private const val TAG = "YamlGenerator"
    
    /**
     * 生成位置数据YAML文件
     */
    fun generateLocationDataYaml(
        context: Context,
        locations: List<LocationEntity>,
        outputStream: FileOutputStream
    ): Boolean {
        return try {
            val yamlContent = buildYamlData(context, locations)
            outputStream.write(yamlContent.toByteArray())
            outputStream.flush()
            outputStream.close()
            
            Logger.i(TAG, "YAML data generated successfully with ${locations.size} locations")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate YAML data", e)
            false
        }
    }
    
    /**
     * 构建YAML数据
     */
    private fun buildYamlData(context: Context, locations: List<LocationEntity>): String {
        val yaml = StringBuilder()
        
        // YAML文档开始
        yaml.append("---\n")
        
        // 文档信息
        yaml.append("# MQTT位置跟踪器导出数据\n")
        yaml.append("document:\n")
        yaml.append("  generator: \"MQTT位置跟踪器\"\n")
        yaml.append("  version: \"1.0\"\n")
        yaml.append("  generated: \"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\"\n")
        yaml.append("  total_locations: ${locations.size}\n")
        yaml.append("  date_range:\n")
        yaml.append("    start: \"${getDateRangeStart(locations)}\"\n")
        yaml.append("    end: \"${getDateRangeEnd(locations)}\"\n")
        yaml.append("    days: ${getDateRangeDays(locations)}\n\n")
        
        // 统计信息
        yaml.append("statistics:\n")
        yaml.append("  average_accuracy: ${getAverageAccuracy(locations)}\n")
        yaml.append("  max_speed: ${getMaxSpeed(locations)}\n")
        yaml.append("  max_altitude: ${getMaxAltitude(locations)}\n")
        yaml.append("  stationary_percentage: ${getStationaryPercentage(locations)}\n\n")
        
        // 位置数据
        yaml.append("locations:\n")
        
        for ((index, location) in locations.withIndex()) {
            yaml.append("  - id: ${location.id}\n")
            yaml.append("    coordinates:\n")
            yaml.append("      latitude: ${location.latitude}\n")
            yaml.append("      longitude: ${location.longitude}\n")
            yaml.append("    timestamp: ${location.timestamp}\n")
            yaml.append("    formatted_time: \"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(location.timestamp))}\"\n")
            
            location.accuracy?.let { 
                yaml.append("    accuracy: $it\n") 
            }
            
            location.altitude?.let { 
                yaml.append("    altitude: $it\n") 
            }
            
            location.speed?.let { 
                yaml.append("    speed: $it\n") 
            }
            
            location.bearing?.let { 
                yaml.append("    bearing: $it\n") 
            }
            
            yaml.append("    synced: ${location.isSynced}\n")
            yaml.append("    created_at: ${location.createdAt}\n")
            
            // 添加分割线（除了最后一个元素）
            if (index < locations.size - 1) {
                yaml.append("\n")
            }
        }
        
        // YAML文档结束
        yaml.append("...\n")
        
        return yaml.toString()
    }
    
    /**
     * 获取数据范围开始时间
     */
    private fun getDateRangeStart(locations: List<LocationEntity>): String {
        if (locations.isEmpty()) return ""
        
        val minTime = locations.minOfOrNull { it.timestamp } ?: return ""
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(minTime))
    }
    
    /**
     * 获取数据范围结束时间
     */
    private fun getDateRangeEnd(locations: List<LocationEntity>): String {
        if (locations.isEmpty()) return ""
        
        val maxTime = locations.maxOfOrNull { it.timestamp } ?: return ""
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(maxTime))
    }
    
    /**
     * 获取数据范围天数
     */
    private fun getDateRangeDays(locations: List<LocationEntity>): Int {
        if (locations.isEmpty()) return 0
        
        val timestamps = locations.map { it.timestamp }
        val minTime = timestamps.minOrNull() ?: return 0
        val maxTime = timestamps.maxOrNull() ?: return 0
        
        return ((maxTime - minTime) / (1000 * 60 * 60 * 24)).toInt()
    }
    
    /**
     * 获取平均精度
     */
    private fun getAverageAccuracy(locations: List<LocationEntity>): String {
        val accuracies = locations.mapNotNull { it.accuracy }
        if (accuracies.isEmpty()) return "0.0"
        
        val avg = accuracies.average()
        return String.format("%.2f", avg)
    }
    
    /**
     * 获取最大速度
     */
    private fun getMaxSpeed(locations: List<LocationEntity>): String {
        val speeds = locations.mapNotNull { it.speed }
        if (speeds.isEmpty()) return "0.0"
        
        val max = speeds.maxOrNull() ?: 0f
        return String.format("%.2f", max)
    }
    
    /**
     * 获取最高海拔
     */
    private fun getMaxAltitude(locations: List<LocationEntity>): String {
        val altitudes = locations.mapNotNull { it.altitude }
        if (altitudes.isEmpty()) return "0.0"
        
        val max = altitudes.maxOrNull() ?: 0.0
        return String.format("%.2f", max)
    }
    
    /**
     * 获取静止记录占比
     */
    private fun getStationaryPercentage(locations: List<LocationEntity>): String {
        val speeds = locations.mapNotNull { it.speed }
        if (speeds.isEmpty()) return "0.0"
        
        val stationaryCount = speeds.count { it < 0.5f } // 速度小于0.5m/s认为是静止
        val percentage = (stationaryCount.toDouble() / speeds.size) * 100
        return String.format("%.1f", percentage)
    }
    
    /**
     * 生成YAML示例模板
     */
    fun generateYamlTemplate(): String {
        return """
# MQTT位置跟踪器YAML数据模板
---
document:
  generator: "MQTT位置跟踪器"
  version: "1.0"
  generated: "2023-01-01 12:00:00"
  total_locations: 0
  date_range:
    start: "2023-01-01"
    end: "2023-01-01"
    days: 0

statistics:
  average_accuracy: 0.0
  max_speed: 0.0
  max_altitude: 0.0
  stationary_percentage: 0.0

locations:
  - id: 1
    coordinates:
      latitude: 39.9042
      longitude: 116.4074
    timestamp: 1672531200000
    formatted_time: "2023-01-01 00:00:00"
    accuracy: 5.0
    altitude: 43.5
    speed: 0.0
    bearing: 0.0
    synced: true
    created_at: 1672531200000
...
        """.trimIndent()
    }
}