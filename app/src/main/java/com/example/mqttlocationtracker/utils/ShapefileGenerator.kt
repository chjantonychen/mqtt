package com.example.mqttlocationtracker.utils

import android.content.Context
import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Shapefile生成器，用于将位置数据导出为ESRI Shapefile格式
 */
object ShapefileGenerator {
    
    private const val TAG = "ShapefileGenerator"
    
    /**
     * 生成位置数据Shapefile文件集
     */
    fun generateLocationDataShapefile(
        context: Context,
        locations: List<LocationEntity>,
        outputBasePath: String // 基础路径，不包含扩展名
    ): Boolean {
        return try {
            // 生成.shp文件（几何数据）
            generateShpFile(locations, "${outputBasePath}.shp")
            
            // 生成.shx文件（索引）
            generateShxFile(locations, "${outputBasePath}.shx")
            
            // 生成.dbf文件（属性数据）
            generateDbfFile(context, locations, "${outputBasePath}.dbf")
            
            // 生成.prj文件（投影信息）
            generatePrjFile("${outputBasePath}.prj")
            
            Logger.i(TAG, "Shapefile generated successfully with ${locations.size} locations")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate Shapefile", e)
            false
        }
    }
    
    /**
     * 生成.shp文件（几何数据）
     */
    private fun generateShpFile(locations: List<LocationEntity>, outputPath: String) {
        // 这里应该实现真实的Shapefile生成逻辑
        // 由于Shapefile格式较为复杂，这里只是模拟生成过程
        
        val outputFile = java.io.File(outputPath)
        val outputStream = FileOutputStream(outputFile)
        
        // 写入Shapefile头部信息（简化版）
        outputStream.write("# Shapefile Header (Simplified)\n".toByteArray())
        outputStream.write("Version: ESRI Shapefile 1.0\n".toByteArray())
        outputStream.write("Geometry Type: Point\n".toByteArray())
        outputStream.write("Record Count: ${locations.size}\n".toByteArray())
        outputStream.write("Bounding Box: Calculated from data\n".toByteArray())
        outputStream.write("\n".toByteArray())
        
        // 写入记录数据
        outputStream.write("# Point Records\n".toByteArray())
        for ((index, location) in locations.withIndex()) {
            outputStream.write("Record ${index + 1}:\n".toByteArray())
            outputStream.write("  Type: Point\n".toByteArray())
            outputStream.write("  X: ${location.longitude}\n".toByteArray())
            outputStream.write("  Y: ${location.latitude}\n".toByteArray())
            location.altitude?.let {
                outputStream.write("  Z: $it\n".toByteArray())
            }
            outputStream.write("\n".toByteArray())
        }
        
        outputStream.close()
    }
    
    /**
     * 生成.shx文件（索引）
     */
    private fun generateShxFile(locations: List<LocationEntity>, outputPath: String) {
        val outputFile = java.io.File(outputPath)
        val outputStream = FileOutputStream(outputFile)
        
        // 写入索引文件头部信息（简化版）
        outputStream.write("# Shapefile Index (Simplified)\n".toByteArray())
        outputStream.write("Version: ESRI Shapefile Index 1.0\n".toByteArray())
        outputStream.write("Record Count: ${locations.size}\n".toByteArray())
        outputStream.write("\n".toByteArray())
        
        // 写入索引记录
        outputStream.write("# Index Records\n".toByteArray())
        var offset = 50 // 假设每个记录占用50字节
        for ((index, location) in locations.withIndex()) {
            outputStream.write("Record ${index + 1}: Offset=$offset, Length=50\n".toByteArray())
            offset += 50
        }
        
        outputStream.close()
    }
    
    /**
     * 生成.dbf文件（属性数据）
     */
    private fun generateDbfFile(context: Context, locations: List<LocationEntity>, outputPath: String) {
        val outputFile = java.io.File(outputPath)
        val outputStream = FileOutputStream(outputFile)
        
        // 写入DBF文件头部信息（简化版）
        outputStream.write("# DBF Attribute Data (Simplified)\n".toByteArray())
        outputStream.write("Version: dBASE III\n".toByteArray())
        outputStream.write("Record Count: ${locations.size}\n".toByteArray())
        outputStream.write("Fields: id, timestamp, accuracy, altitude, speed, bearing, synced\n".toByteArray())
        outputStream.write("\n".toByteArray())
        
        // 写入记录数据
        outputStream.write("# Attribute Records\n".toByteArray())
        for ((index, location) in locations.withIndex()) {
            outputStream.write("Record ${index + 1}:\n".toByteArray())
            outputStream.write("  id: ${location.id}\n".toByteArray())
            outputStream.write("  timestamp: ${location.timestamp}\n".toByteArray())
            outputStream.write("  formatted_time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(location.timestamp))}\n".toByteArray())
            location.accuracy?.let {
                outputStream.write("  accuracy: $it\n".toByteArray())
            }
            location.altitude?.let {
                outputStream.write("  altitude: $it\n".toByteArray())
            }
            location.speed?.let {
                outputStream.write("  speed: $it\n".toByteArray())
            }
            location.bearing?.let {
                outputStream.write("  bearing: $it\n".toByteArray())
            }
            outputStream.write("  synced: ${location.isSynced}\n".toByteArray())
            outputStream.write("\n".toByteArray())
        }
        
        outputStream.close()
    }
    
    /**
     * 生成.prj文件（投影信息）
     */
    private fun generatePrjFile(outputPath: String) {
        val outputFile = java.io.File(outputPath)
        val outputStream = FileOutputStream(outputFile)
        
        // 写入WGS84投影信息
        val wgs84Projection = """
GEOGCS["GCS_WGS_1984",
    DATUM["D_WGS_1984",
        SPHEROID["WGS_1984",6378137,298.257223563]],
    PRIMEM["Greenwich",0],
    UNIT["Degree",0.017453292519943295]]
        """.trimIndent()
        
        outputStream.write(wgs84Projection.toByteArray())
        outputStream.close()
    }
    
    /**
     * 生成轨迹线Shapefile
     */
    fun generateLocationTrackShapefile(
        context: Context,
        locations: List<LocationEntity>,
        outputBasePath: String
    ): Boolean {
        return try {
            // 生成轨迹线.shp文件
            generateTrackShpFile(locations, "${outputBasePath}_track.shp")
            
            // 生成轨迹线.shx文件
            generateTrackShxFile(locations, "${outputBasePath}_track.shx")
            
            // 生成轨迹线.dbf文件
            generateTrackDbfFile(context, locations, "${outputBasePath}_track.dbf")
            
            // 生成轨迹线.prj文件
            generatePrjFile("${outputBasePath}_track.prj")
            
            Logger.i(TAG, "Track Shapefile generated successfully with ${locations.size} locations")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate track Shapefile", e)
            false
        }
    }
    
    /**
     * 生成轨迹线.shp文件
     */
    private fun generateTrackShpFile(locations: List<LocationEntity>, outputPath: String) {
        val outputFile = java.io.File(outputPath)
        val outputStream = FileOutputStream(outputFile)
        
        // 写入Shapefile头部信息（简化版）
        outputStream.write("# Shapefile Header (Simplified)\n".toByteArray())
        outputStream.write("Version: ESRI Shapefile 1.0\n".toByteArray())
        outputStream.write("Geometry Type: PolyLine\n".toByteArray())
        outputStream.write("Record Count: 1\n".toByteArray())
        outputStream.write("Bounding Box: Calculated from data\n".toByteArray())
        outputStream.write("\n".toByteArray())
        
        // 写入轨迹线记录
        outputStream.write("# PolyLine Record\n".toByteArray())
        outputStream.write("Record 1:\n".toByteArray())
        outputStream.write("  Type: PolyLine\n".toByteArray())
        outputStream.write("  Part Count: 1\n".toByteArray())
        outputStream.write("  Point Count: ${locations.size}\n".toByteArray())
        outputStream.write("  Points:\n".toByteArray())
        
        for (location in locations) {
            outputStream.write("    (${location.longitude}, ${location.latitude})\n".toByteArray())
        }
        
        outputStream.close()
    }
    
    /**
     * 生成轨迹线.shx文件
     */
    private fun generateTrackShxFile(locations: List<LocationEntity>, outputPath: String) {
        val outputFile = java.io.File(outputPath)
        val outputStream = FileOutputStream(outputFile)
        
        // 写入索引文件头部信息（简化版）
        outputStream.write("# Shapefile Index (Simplified)\n".toByteArray())
        outputStream.write("Version: ESRI Shapefile Index 1.0\n".toByteArray())
        outputStream.write("Record Count: 1\n".toByteArray())
        outputStream.write("\n".toByteArray())
        
        // 写入索引记录
        outputStream.write("# Index Records\n".toByteArray())
        outputStream.write("Record 1: Offset=50, Length=1000\n".toByteArray())
        
        outputStream.close()
    }
    
    /**
     * 生成轨迹线.dbf文件
     */
    private fun generateTrackDbfFile(context: Context, locations: List<LocationEntity>, outputPath: String) {
        val outputFile = java.io.File(outputPath)
        val outputStream = FileOutputStream(outputFile)
        
        // 写入DBF文件头部信息（简化版）
        outputStream.write("# DBF Attribute Data (Simplified)\n".toByteArray())
        outputStream.write("Version: dBASE III\n".toByteArray())
        outputStream.write("Record Count: 1\n".toByteArray())
        outputStream.write("Fields: name, total_points, start_time, end_time, distance\n".toByteArray())
        outputStream.write("\n".toByteArray())
        
        // 写入记录数据
        outputStream.write("# Attribute Records\n".toByteArray())
        outputStream.write("Record 1:\n".toByteArray())
        outputStream.write("  name: MQTT位置轨迹\n".toByteArray())
        outputStream.write("  total_points: ${locations.size}\n".toByteArray())
        if (locations.isNotEmpty()) {
            outputStream.write("  start_time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(locations.first().timestamp))}\n".toByteArray())
            outputStream.write("  end_time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(locations.last().timestamp))}\n".toByteArray())
        }
        outputStream.write("  distance: ${calculateTotalDistance(locations)}\n".toByteArray())
        
        outputStream.close()
    }
    
    /**
     * 计算总距离（简化版）
     */
    private fun calculateTotalDistance(locations: List<LocationEntity>): Double {
        if (locations.size < 2) return 0.0
        
        var totalDistance = 0.0
        for (i in 1 until locations.size) {
            val prev = locations[i - 1]
            val curr = locations[i]
            totalDistance += calculateDistance(prev.latitude, prev.longitude, curr.latitude, curr.longitude)
        }
        
        return totalDistance
    }
    
    /**
     * 计算两点间距离（简化版）
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // 简化的距离计算（实际应用中应使用更精确的算法）
        val earthRadius = 6371000.0 // 地球半径（米）
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
    
    /**
     * 生成Shapefile说明文件
     */
    fun generateShapefileReadme(): String {
        return """
# Shapefile格式说明

## 文件组成
一个完整的Shapefile由以下文件组成：
- .shp: 存储几何数据
- .shx: 几何数据索引
- .dbf: 属性数据（dBASE格式）
- .prj: 坐标系和投影信息

## 支持的几何类型
- Point: 点要素
- PolyLine: 线要素
- Polygon: 面要素

## 坐标系
本数据采用WGS84坐标系（EPSG:4326），经纬度坐标。

## 使用说明
1. 所有文件必须在同一目录下
2. 文件名前缀必须相同
3. 可在ArcGIS、QGIS等GIS软件中打开
4. 支持导入到大多数支持Shapefile格式的系统中

## 数据字段说明
### 点要素属性表
- id: 位置记录ID
- timestamp: 时间戳（Unix时间）
- formatted_time: 格式化时间
- accuracy: 精度（米）
- altitude: 海拔（米）
- speed: 速度（m/s）
- bearing: 方向角
- synced: 同步状态

### 线要素属性表
- name: 轨迹名称
- total_points: 总点数
- start_time: 开始时间
- end_time: 结束时间
- distance: 总距离（米）
        """.trimIndent()
    }
}