package com.example.mqttlocationtracker.utils

import android.content.Context
import android.util.Log
import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.io.*
import java.text.SimpleDateFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.Date

/**
 * ZIP压缩包生成器，用于将位置数据打包成ZIP文件
 */
object ZipGenerator {
    
    private const val TAG = "ZipGenerator"
    
    /**
     * 生成包含多种格式的位置数据ZIP包
     */
    fun generateLocationDataZip(
        context: Context,
        locations: List<LocationEntity>,
        outputStream: FileOutputStream,
        includeFormats: Set<DataFormat> = setOf(
            DataFormat.CSV,
            DataFormat.JSON,
            DataFormat.KML,
            DataFormat.GPX
        )
    ): Boolean {
        return try {
            val zipOutputStream = ZipOutputStream(BufferedOutputStream(outputStream))
            
            // 生成README文件
            addReadmeFile(zipOutputStream, locations)
            
            // 根据要求的格式生成文件
            if (includeFormats.contains(DataFormat.CSV)) {
                addCsvFile(zipOutputStream, locations)
            }
            
            if (includeFormats.contains(DataFormat.JSON)) {
                addJsonFile(zipOutputStream, locations)
            }
            
            if (includeFormats.contains(DataFormat.KML)) {
                addKmlFile(zipOutputStream, locations)
            }
            
            if (includeFormats.contains(DataFormat.GPX)) {
                addGpxFile(zipOutputStream, locations)
            }
            
            if (includeFormats.contains(DataFormat.EXCEL)) {
                addExcelFile(zipOutputStream, locations)
            }
            
            zipOutputStream.close()
            Logger.i(TAG, "Location data ZIP package generated successfully")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate location data ZIP package", e)
            false
        }
    }
    
    /**
     * 添加README文件
     */
    private fun addReadmeFile(zipOutputStream: ZipOutputStream, locations: List<LocationEntity>) {
        try {
            val entry = ZipEntry("README.txt")
            zipOutputStream.putNextEntry(entry)
            
            val readmeContent = """
                MQTT位置跟踪器数据导出包
                ========================
                
                生成时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(Date())}
                位置点总数: ${locations.size}
                
                包含文件说明:
                - locations.csv: CSV格式的位置数据
                - locations.json: JSON格式的位置数据
                - locations.kml: KML格式的位置数据（可在Google Earth中打开）
                - locations.gpx: GPX格式的位置数据（可在GPS设备中使用）
                - locations_excel.csv: Excel兼容的CSV格式
                
                如需导入数据，请选择相应的文件格式。
                
                由MQTT位置跟踪器生成
                https://github.com/your-repo/mqtt-location-tracker
            """.trimIndent()
            
            zipOutputStream.write(readmeContent.toByteArray())
            zipOutputStream.closeEntry()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to add README file to ZIP", e)
        }
    }
    
    /**
     * 添加CSV文件
     */
    private fun addCsvFile(zipOutputStream: ZipOutputStream, locations: List<LocationEntity>) {
        try {
            val entry = ZipEntry("locations.csv")
            zipOutputStream.putNextEntry(entry)
            
            // 写入CSV头部
            val header = "timestamp,latitude,longitude,accuracy,altitude,speed,bearing,is_synced\n"
            zipOutputStream.write(header.toByteArray())
            
            // 写入数据行
            for (location in locations) {
                val row = "${location.timestamp},${location.latitude},${location.longitude}," +
                        "${location.accuracy ?: ""},${location.altitude ?: ""}," +
                        "${location.speed ?: ""},${location.bearing ?: ""},${location.isSynced}\n"
                zipOutputStream.write(row.toByteArray())
            }
            
            zipOutputStream.closeEntry()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to add CSV file to ZIP", e)
        }
    }
    
    /**
     * 添加JSON文件
     */
    private fun addJsonFile(zipOutputStream: ZipOutputStream, locations: List<LocationEntity>) {
        try {
            val entry = ZipEntry("locations.json")
            zipOutputStream.putNextEntry(entry)
            
            // 构建JSON数组
            val jsonBuilder = StringBuilder()
            jsonBuilder.append("[\n")
            
            locations.forEachIndexed { index, location ->
                if (index > 0) jsonBuilder.append(",\n")
                jsonBuilder.append("  {\n")
                jsonBuilder.append("    \"id\": ${location.id},\n")
                jsonBuilder.append("    \"latitude\": ${location.latitude},\n")
                jsonBuilder.append("    \"longitude\": ${location.longitude},\n")
                jsonBuilder.append("    \"timestamp\": ${location.timestamp}")
                
                location.accuracy?.let { jsonBuilder.append(",\n    \"accuracy\": $it") }
                location.altitude?.let { jsonBuilder.append(",\n    \"altitude\": $it") }
                location.speed?.let { jsonBuilder.append(",\n    \"speed\": $it") }
                location.bearing?.let { jsonBuilder.append(",\n    \"bearing\": $it") }
                
                jsonBuilder.append(",\n    \"is_synced\": ${location.isSynced}")
                jsonBuilder.append("\n  }")
            }
            
            jsonBuilder.append("\n]")
            
            zipOutputStream.write(jsonBuilder.toString().toByteArray())
            zipOutputStream.closeEntry()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to add JSON file to ZIP", e)
        }
    }
    
    /**
     * 添加KML文件
     */
    private fun addKmlFile(zipOutputStream: ZipOutputStream, locations: List<LocationEntity>) {
        try {
            val entry = ZipEntry("locations.kml")
            zipOutputStream.putNextEntry(entry)
            
            // 生成KML头部
            val kmlHeader = """<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>
    <name>MQTT位置跟踪数据</name>
    <description>由MQTT位置跟踪器导出的位置数据</description>
"""
            zipOutputStream.write(kmlHeader.toByteArray())
            
            // 生成Placemark数据
            locations.forEachIndexed { index, location ->
                val timeString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(Date(location.timestamp))
                
                val placemark = """
    <Placemark>
        <name>位置点 #$index</name>
        <TimeStamp>
            <when>$timeString</when>
        </TimeStamp>
        <Point>
            <coordinates>${location.longitude},${location.latitude}${if (location.altitude != null) ",${location.altitude}" else ""}</coordinates>
        </Point>
    </Placemark>
"""
                zipOutputStream.write(placemark.toByteArray())
            }
            
            // 生成KML尾部
            val kmlFooter = """</Document>
</kml>"""
            zipOutputStream.write(kmlFooter.toByteArray())
            
            zipOutputStream.closeEntry()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to add KML file to ZIP", e)
        }
    }
    
    /**
     * 添加GPX文件
     */
    private fun addGpxFile(zipOutputStream: ZipOutputStream, locations: List<LocationEntity>) {
        try {
            val entry = ZipEntry("locations.gpx")
            zipOutputStream.putNextEntry(entry)
            
            // 生成GPX头部
            val gpxHeader = """<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="MQTT位置跟踪器" xmlns="http://www.topografix.com/GPX/1/1">
  <trk>
    <name>MQTT位置跟踪数据</name>
    <desc>由MQTT位置跟踪器导出的位置数据</desc>
    <trkseg>
"""
            zipOutputStream.write(gpxHeader.toByteArray())
            
            // 生成TrackPoint数据
            for (location in locations) {
                val timeString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(Date(location.timestamp))
                
                val trackPoint = """
      <trkpt lat="${location.latitude}" lon="${location.longitude}">
        <time>$timeString</time>
${if (location.altitude != null) "        <ele>${location.altitude}</ele>\n" else ""}
${if (location.accuracy != null) "        <hdop>${location.accuracy}</hdop>\n" else ""}
${if (location.speed != null) "        <speed>${location.speed}</speed>\n" else ""}
      </trkpt>
"""
                zipOutputStream.write(trackPoint.toByteArray())
            }
            
            // 生成GPX尾部
            val gpxFooter = """    </trkseg>
  </trk>
</gpx>"""
            zipOutputStream.write(gpxFooter.toByteArray())
            
            zipOutputStream.closeEntry()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to add GPX file to ZIP", e)
        }
    }
    
    /**
     * 添加Excel兼容的CSV文件
     */
    private fun addExcelFile(zipOutputStream: ZipOutputStream, locations: List<LocationEntity>) {
        try {
            val entry = ZipEntry("locations_excel.csv")
            zipOutputStream.putNextEntry(entry)
            
            // 写入Excel兼容的CSV头部
            val header = "时间,纬度,经度,精度(米),海拔(米),速度(m/s),同步状态\n"
            zipOutputStream.write(header.toByteArray())
            
            // 写入数据行
            for (location in locations) {
                val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(Date(location.timestamp))
                val syncedStr = if (location.isSynced) "已同步" else "未同步"
                
                val row = "$timeStr,${location.latitude},${location.longitude}," +
                        "${location.accuracy ?: ""},${location.altitude ?: ""}," +
                        "${location.speed ?: ""},$syncedStr\n"
                zipOutputStream.write(row.toByteArray())
            }
            
            zipOutputStream.closeEntry()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to add Excel file to ZIP", e)
        }
    }
    
    /**
     * 数据格式枚举
     */
    enum class DataFormat {
        CSV,
        JSON,
        KML,
        GPX,
        EXCEL
    }
}