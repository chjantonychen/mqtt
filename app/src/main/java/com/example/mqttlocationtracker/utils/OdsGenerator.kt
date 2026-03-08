package com.example.mqttlocationtracker.utils

import android.content.Context
import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * ODS生成器，用于将位置数据导出为OpenDocument Spreadsheet格式
 */
object OdsGenerator {
    
    private const val TAG = "OdsGenerator"
    
    /**
     * 生成位置数据ODS文件
     */
    fun generateLocationDataOds(
        context: Context,
        locations: List<LocationEntity>,
        outputStream: FileOutputStream
    ): Boolean {
        return try {
            val odsContent = buildOdsContent(context, locations)
            outputStream.write(odsContent.toByteArray())
            outputStream.flush()
            outputStream.close()
            
            Logger.i(TAG, "ODS data generated successfully with ${locations.size} locations")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate ODS data", e)
            false
        }
    }
    
    /**
     * 构建ODS内容（简化版）
     */
    private fun buildOdsContent(context: Context, locations: List<LocationEntity>): String {
        val ods = StringBuilder()
        
        // ODS文件实际上是一个ZIP压缩包，包含多个XML文件
        // 这里我们生成一个简化的XML表示形式
        
        // MIME类型声明
        ods.append("MimeType: application/vnd.oasis.opendocument.spreadsheet\n\n")
        
        // 内容.xml（简化版）
        ods.append("content.xml:\n")
        ods.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        ods.append("<office:document-content ")
        ods.append("xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" ")
        ods.append("xmlns:table=\"urn:oasis:names:tc:opendocument:xmlns:table:1.0\" ")
        ods.append("xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\">\n")
        
        ods.append("  <office:body>\n")
        ods.append("    <office:spreadsheet>\n")
        ods.append("      <table:table table:name=\"位置数据\">\n")
        
        // 表头
        ods.append("        <table:table-row>\n")
        ods.append("          <table:table-cell office:value-type=\"string\"><text:p>时间</text:p></table:table-cell>\n")
        ods.append("          <table:table-cell office:value-type=\"string\"><text:p>纬度</text:p></table:table-cell>\n")
        ods.append("          <table:table-cell office:value-type=\"string\"><text:p>经度</text:p></table:table-cell>\n")
        ods.append("          <table:table-cell office:value-type=\"string\"><text:p>精度(米)</text:p></table:table-cell>\n")
        ods.append("          <table:table-cell office:value-type=\"string\"><text:p>海拔(米)</text:p></table:table-cell>\n")
        ods.append("          <table:table-cell office:value-type=\"string\"><text:p>速度(m/s)</text:p></table:table-cell>\n")
        ods.append("          <table:table-cell office:value-type=\"string\"><text:p>方向角</text:p></table:table-cell>\n")
        ods.append("          <table:table-cell office:value-type=\"string\"><text:p>同步状态</text:p></table:table-cell>\n")
        ods.append("        </table:table-row>\n")
        
        // 数据行
        for (location in locations) {
            ods.append("        <table:table-row>\n")
            
            // 时间
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(location.timestamp))
            ods.append("          <table:table-cell office:value-type=\"date\" office:date-value=\"$timeStr\">")
            ods.append("<text:p>$timeStr</text:p></table:table-cell>\n")
            
            // 纬度
            ods.append("          <table:table-cell office:value-type=\"float\" office:value=\"${location.latitude}\">")
            ods.append("<text:p>${location.latitude}</text:p></table:table-cell>\n")
            
            // 经度
            ods.append("          <table:table-cell office:value-type=\"float\" office:value=\"${location.longitude}\">")
            ods.append("<text:p>${location.longitude}</text:p></table:table-cell>\n")
            
            // 精度
            val accuracy = location.accuracy ?: 0f
            ods.append("          <table:table-cell office:value-type=\"float\" office:value=\"$accuracy\">")
            ods.append("<text:p>${if (location.accuracy != null) location.accuracy else ""}</text:p></table:table-cell>\n")
            
            // 海拔
            val altitude = location.altitude ?: 0.0
            ods.append("          <table:table-cell office:value-type=\"float\" office:value=\"$altitude\">")
            ods.append("<text:p>${if (location.altitude != null) location.altitude else ""}</text:p></table:table-cell>\n")
            
            // 速度
            val speed = location.speed ?: 0f
            ods.append("          <table:table-cell office:value-type=\"float\" office:value=\"$speed\">")
            ods.append("<text:p>${if (location.speed != null) location.speed else ""}</text:p></table:table-cell>\n")
            
            // 方向角
            val bearing = location.bearing ?: 0f
            ods.append("          <table:table-cell office:value-type=\"float\" office:value=\"$bearing\">")
            ods.append("<text:p>${if (location.bearing != null) location.bearing else ""}</text:p></table:table-cell>\n")
            
            // 同步状态
            val syncedText = if (location.isSynced) "已同步" else "未同步"
            ods.append("          <table:table-cell office:value-type=\"string\">")
            ods.append("<text:p>$syncedText</text:p></table:table-cell>\n")
            
            ods.append("        </table:table-row>\n")
        }
        
        ods.append("      </table:table>\n")
        ods.append("    </office:spreadsheet>\n")
        ods.append("  </office:body>\n")
        ods.append("</office:document-content>\n\n")
        
        // meta.xml（简化版）
        ods.append("meta.xml:\n")
        ods.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        ods.append("<office:document-meta ")
        ods.append("xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" ")
        ods.append("xmlns:meta=\"urn:oasis:names:tc:opendocument:xmlns:meta:1.0\">\n")
        ods.append("  <office:meta>\n")
        ods.append("    <meta:generator>MQTT位置跟踪器</meta:generator>\n")
        ods.append("    <meta:creation-date>${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())}</meta:creation-date>\n")
        ods.append("    <meta:document-statistic meta:table-count=\"1\" meta:cell-count=\"${locations.size * 8}\" meta:object-count=\"0\"/>\n")
        ods.append("  </office:meta>\n")
        ods.append("</office:document-meta>\n\n")
        
        // styles.xml（简化版）
        ods.append("styles.xml:\n")
        ods.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        ods.append("<office:document-styles ")
        ods.append("xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" ")
        ods.append("xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\">\n")
        ods.append("  <office:styles>\n")
        ods.append("    <style:style style:name=\"Default\" style:family=\"table-cell\">\n")
        ods.append("      <style:table-cell-properties style:decimal-places=\"2\"/>\n")
        ods.append("    </style:style>\n")
        ods.append("  </office:styles>\n")
        ods.append("</office:document-styles>\n")
        
        return ods.toString()
    }
    
    /**
     * 生成带图表的ODS文件
     */
    fun generateLocationDataOdsWithCharts(
        context: Context,
        locations: List<LocationEntity>,
        outputStream: FileOutputStream
    ): Boolean {
        return try {
            val odsContent = buildOdsContentWithCharts(context, locations)
            outputStream.write(odsContent.toByteArray())
            outputStream.flush()
            outputStream.close()
            
            Logger.i(TAG, "ODS data with charts generated successfully with ${locations.size} locations")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate ODS data with charts", e)
            false
        }
    }
    
    /**
     * 构建带图表的ODS内容（简化版）
     */
    private fun buildOdsContentWithCharts(context: Context, locations: List<LocationEntity>): String {
        val ods = buildOdsContent(context, locations)
        
        // 添加图表信息（简化版）
        val charts = StringBuilder()
        charts.append("\n\n# Charts (Simplified representation)\n")
        charts.append("charts/chart1.xml:\n")
        charts.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        charts.append("<chart:chart chart:class=\"chart:scatter\">\n")
        charts.append("  <chart:title>位置轨迹图</chart:title>\n")
        charts.append("  <chart:plot-area>\n")
        charts.append("    <chart:series chart:values-cell-range-address=\"位置数据.B1:C${locations.size + 1}\"/>\n")
        charts.append("  </chart:plot-area>\n")
        charts.append("</chart:chart>\n")
        
        return ods + charts.toString()
    }
    
    /**
     * 生成ODS模板
     */
    fun generateOdsTemplate(): String {
        return """
# ODS模板文件结构说明

## 文件组成
ODS文件是一个ZIP压缩包，包含以下文件：
- mimetype: MIME类型声明
- content.xml: 电子表格内容
- meta.xml: 文档元数据
- styles.xml: 样式信息
- settings.xml: 应用设置
- Thumbnails/: 缩略图目录
- Configurations2/: 配置信息

## content.xml结构示例
<?xml version="1.0" encoding="UTF-8"?>
<office:document-content xmlns:office="..." xmlns:table="...">
  <office:body>
    <office:spreadsheet>
      <table:table table:name="Sheet1">
        <table:table-row>
          <table:table-cell office:value-type="string">
            <text:p>列标题</text:p>
          </table:table-cell>
        </table:table-row>
      </table:table>
    </office:spreadsheet>
  </office:body>
</office:document-content>

## 支持的数据类型
- 字符串 (string)
- 数字 (float)
- 日期 (date)
- 布尔值 (boolean)
- 货币 (currency)

## 样式支持
- 字体样式
- 背景色
- 边框
- 对齐方式
- 数字格式

## 图表支持
- 散点图
- 折线图
- 柱状图
- 饼图
        """.trimIndent()
    }
}