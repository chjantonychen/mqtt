package com.example.mqttlocationtracker.utils

import android.content.Context
import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * XML生成器，用于将位置数据导出为XML格式
 */
object XmlGenerator {
    
    private const val TAG = "XmlGenerator"
    
    /**
     * 生成位置数据XML文件
     */
    fun generateLocationDataXml(
        context: Context,
        locations: List<LocationEntity>,
        outputStream: FileOutputStream
    ): Boolean {
        return try {
            val xmlContent = buildXmlData(context, locations)
            outputStream.write(xmlContent.toByteArray())
            outputStream.flush()
            outputStream.close()
            
            Logger.i(TAG, "XML data generated successfully with ${locations.size} locations")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate XML data", e)
            false
        }
    }
    
    /**
     * 构建XML数据
     */
    private fun buildXmlData(context: Context, locations: List<LocationEntity>): String {
        val xml = StringBuilder()
        
        // XML声明和根元素
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        xml.append("<locationData xmlns=\"http://mqtt-location-tracker.example.com/schema\">\n")
        
        // 元数据
        xml.append("  <metadata>\n")
        xml.append("    <generator>MQTT位置跟踪器</generator>\n")
        xml.append("    <version>1.0</version>\n")
        xml.append("    <generated>${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())}</generated>\n")
        xml.append("    <totalLocations>${locations.size}</totalLocations>\n")
        xml.append("    <dateRange>\n")
        xml.append("      <start>${getDateRangeStart(locations)}</start>\n")
        xml.append("      <end>${getDateRangeEnd(locations)}</end>\n")
        xml.append("    </dateRange>\n")
        xml.append("  </metadata>\n\n")
        
        // 统计信息
        xml.append("  <statistics>\n")
        xml.append("    <averageAccuracy>${getAverageAccuracy(locations)}</averageAccuracy>\n")
        xml.append("    <maxSpeed>${getMaxSpeed(locations)}</maxSpeed>\n")
        xml.append("    <maxAltitude>${getMaxAltitude(locations)}</maxAltitude>\n")
        xml.append("    <dataRangeDays>${getDateRangeDays(locations)}</dataRangeDays>\n")
        xml.append("  </statistics>\n\n")
        
        // 位置数据
        xml.append("  <locations>\n")
        
        for (location in locations) {
            xml.append("    <location>\n")
            xml.append("      <id>${location.id}</id>\n")
            xml.append("      <latitude>${location.latitude}</latitude>\n")
            xml.append("      <longitude>${location.longitude}</longitude>\n")
            xml.append("      <timestamp>${location.timestamp}</timestamp>\n")
            xml.append("      <formattedTime>${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(location.timestamp))}</formattedTime>\n")
            
            location.accuracy?.let { 
                xml.append("      <accuracy>$it</accuracy>\n") 
            }
            
            location.altitude?.let { 
                xml.append("      <altitude>$it</altitude>\n") 
            }
            
            location.speed?.let { 
                xml.append("      <speed>$it</speed>\n") 
            }
            
            location.bearing?.let { 
                xml.append("      <bearing>$it</bearing>\n") 
            }
            
            xml.append("      <synced>${location.isSynced}</synced>\n")
            xml.append("      <createdAt>${location.createdAt}</createdAt>\n")
            xml.append("    </location>\n")
        }
        
        xml.append("  </locations>\n")
        
        // 结束根元素
        xml.append("</locationData>")
        
        return xml.toString()
    }
    
    /**
     * 获取数据范围开始时间
     */
    private fun getDateRangeStart(locations: List<LocationEntity>): String {
        if (locations.isEmpty()) return ""
        
        val minTime = locations.minOfOrNull { it.timestamp } ?: return ""
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date(minTime))
    }
    
    /**
     * 获取数据范围结束时间
     */
    private fun getDateRangeEnd(locations: List<LocationEntity>): String {
        if (locations.isEmpty()) return ""
        
        val maxTime = locations.maxOfOrNull { it.timestamp } ?: return ""
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date(maxTime))
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
     * 生成XML Schema定义
     */
    fun generateXmlSchema(): String {
        return """
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           targetNamespace="http://mqtt-location-tracker.example.com/schema"
           xmlns:tns="http://mqtt-location-tracker.example.com/schema"
           elementFormDefault="qualified">

  <xs:element name="locationData">
    <xs:complexType>
      <xs:sequence>
        <xs:element name="metadata" type="tns:MetadataType"/>
        <xs:element name="statistics" type="tns:StatisticsType"/>
        <xs:element name="locations" type="tns:LocationsType"/>
      </xs:sequence>
    </xs:complexType>
  </xs:element>

  <xs:complexType name="MetadataType">
    <xs:sequence>
      <xs:element name="generator" type="xs:string"/>
      <xs:element name="version" type="xs:string"/>
      <xs:element name="generated" type="xs:dateTime"/>
      <xs:element name="totalLocations" type="xs:int"/>
      <xs:element name="dateRange" type="tns:DateRangeType"/>
    </xs:sequence>
  </xs:complexType>

  <xs:complexType name="DateRangeType">
    <xs:sequence>
      <xs:element name="start" type="xs:dateTime"/>
      <xs:element name="end" type="xs:dateTime"/>
    </xs:sequence>
  </xs:complexType>

  <xs:complexType name="StatisticsType">
    <xs:sequence>
      <xs:element name="averageAccuracy" type="xs:decimal"/>
      <xs:element name="maxSpeed" type="xs:decimal"/>
      <xs:element name="maxAltitude" type="xs:decimal"/>
      <xs:element name="dataRangeDays" type="xs:int"/>
    </xs:sequence>
  </xs:complexType>

  <xs:complexType name="LocationsType">
    <xs:sequence>
      <xs:element name="location" type="tns:LocationType" minOccurs="0" maxOccurs="unbounded"/>
    </xs:sequence>
  </xs:complexType>

  <xs:complexType name="LocationType">
    <xs:sequence>
      <xs:element name="id" type="xs:long"/>
      <xs:element name="latitude" type="xs:decimal"/>
      <xs:element name="longitude" type="xs:decimal"/>
      <xs:element name="timestamp" type="xs:long"/>
      <xs:element name="formattedTime" type="xs:string"/>
      <xs:element name="accuracy" type="xs:decimal" minOccurs="0"/>
      <xs:element name="altitude" type="xs:decimal" minOccurs="0"/>
      <xs:element name="speed" type="xs:decimal" minOccurs="0"/>
      <xs:element name="bearing" type="xs:decimal" minOccurs="0"/>
      <xs:element name="synced" type="xs:boolean"/>
      <xs:element name="createdAt" type="xs:long"/>
    </xs:sequence>
  </xs:complexType>

</xs:schema>
        """.trimIndent()
    }
}