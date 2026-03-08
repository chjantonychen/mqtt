package com.example.mqttlocationtracker.utils

import android.content.Context
import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Markdown生成器，用于将位置数据导出为Markdown格式
 */
object MarkdownGenerator {
    
    private const val TAG = "MarkdownGenerator"
    
    /**
     * 生成位置数据Markdown报告
     */
    fun generateLocationReportMarkdown(
        context: Context,
        locations: List<LocationEntity>,
        outputStream: FileOutputStream,
        title: String = "位置跟踪报告"
    ): Boolean {
        return try {
            val markdownContent = buildMarkdownReport(context, locations, title)
            outputStream.write(markdownContent.toByteArray())
            outputStream.flush()
            outputStream.close()
            
            Logger.i(TAG, "Markdown report generated successfully with ${locations.size} locations")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate Markdown report", e)
            false
        }
    }
    
    /**
     * 构建Markdown报告
     */
    private fun buildMarkdownReport(
        context: Context,
        locations: List<LocationEntity>,
        title: String
    ): String {
        val markdown = StringBuilder()
        
        // 标题
        markdown.append("# $title\n\n")
        
        // 生成信息
        markdown.append("## 📋 报告信息\n\n")
        markdown.append("- **生成时间**: ${SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        markdown.append("- **数据来源**: MQTT位置跟踪器\n")
        markdown.append("- **位置点总数**: ${locations.size}\n\n")
        
        // 统计信息
        markdown.append("## 📊 统计信息\n\n")
        markdown.append("| 统计项 | 数值 |\n")
        markdown.append("|--------|------|\n")
        markdown.append("| 总位置点 | ${locations.size} |\n")
        markdown.append("| 数据范围 | ${getDateRangeDays(locations)}天 |\n")
        markdown.append("| 平均精度 | ${getAverageAccuracy(locations)} |\n")
        markdown.append("| 最高速度 | ${getMaxSpeed(locations)} |\n")
        markdown.append("| 最高海拔 | ${getMaxAltitude(locations)} |\n\n")
        
        // 位置轨迹图（占位符）
        markdown.append("## 🗺️ 位置轨迹\n\n")
        markdown.append("> **注意**: 这是一个Markdown格式的报告。在完整实现中，这里将显示位置轨迹图。\n\n")
        markdown.append("```\n")
        markdown.append("位置轨迹图占位符\n")
        markdown.append("```\n\n")
        
        // 详细数据
        markdown.append("## 📋 详细位置数据\n\n")
        
        // 只显示最近的50条记录，避免Markdown过大
        val displayLocations = if (locations.size > 50) {
            locations.sortedByDescending { it.timestamp }.take(50)
        } else {
            locations
        }
        
        markdown.append("| 时间 | 纬度 | 经度 | 精度(米) | 海拔(米) | 速度(m/s) | 状态 |\n")
        markdown.append("|------|------|------|----------|----------|-----------|------|\n")
        
        for (location in displayLocations) {
            markdown.append("| ${SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(location.timestamp))} ")
            markdown.append("| ${String.format("%.6f", location.latitude)} ")
            markdown.append("| ${String.format("%.6f", location.longitude)} ")
            markdown.append("| ${location.accuracy?.let { String.format("%.1f", it) } ?: "-"} ")
            markdown.append("| ${location.altitude?.let { String.format("%.1f", it) } ?: "-"} ")
            markdown.append("| ${location.speed?.let { String.format("%.2f", it) } ?: "-"} ")
            markdown.append("| ${if (location.isSynced) "已同步" else "未同步"} |\n")
        }
        
        // 如果有更多数据，添加提示
        if (locations.size > 50) {
            markdown.append("\n> **提示**: 仅显示最近50条记录，总共${locations.size}条记录\n\n")
        }
        
        // 数据分析
        markdown.append("## 🔍 数据分析\n\n")
        markdown.append("### 时间分布\n\n")
        markdown.append("- 数据采集时间跨度: ${getDateRangeDays(locations)}天\n")
        markdown.append("- 平均每天位置点数: ${getAverageDailyLocations(locations)}\n\n")
        
        markdown.append("### 精度分析\n\n")
        markdown.append("- 最高精度: ${getMinAccuracy(locations)}\n")
        markdown.append("- 最低精度: ${getMaxAccuracy(locations)}\n")
        markdown.append("- 平均精度: ${getAverageAccuracy(locations)}\n\n")
        
        markdown.append("### 速度分析\n\n")
        markdown.append("- 最高速度: ${getMaxSpeed(locations)}\n")
        markdown.append("- 平均速度: ${getAverageSpeed(locations)}\n")
        markdown.append("- 静止记录占比: ${getStationaryPercentage(locations)}%\n\n")
        
        // 使用说明
        markdown.append("## ℹ️ 使用说明\n\n")
        markdown.append("1. 本报告为Markdown格式，可在支持Markdown的编辑器中查看\n")
        markdown.append("2. 表格数据可复制到Excel等电子表格软件中进行进一步分析\n")
        markdown.append("3. 详细的位置轨迹图可在专业GIS软件中查看KML/GPX文件\n")
        markdown.append("4. 如需完整数据，请导出为CSV或数据库格式\n\n")
        
        // 页脚
        markdown.append("---\n\n")
        markdown.append("**MQTT位置跟踪器** - 专业的位置数据管理工具\n\n")
        markdown.append("© ${SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())} MQTT位置跟踪器. 保留所有权利.\n")
        
        return markdown.toString()
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
     * 获取平均每天位置点数
     */
    private fun getAverageDailyLocations(locations: List<LocationEntity>): String {
        if (locations.isEmpty()) return "0"
        
        val days = getDateRangeDays(locations)
        return if (days > 0) {
            "${String.format("%.1f", locations.size.toDouble() / days)}"
        } else {
            "${locations.size}"
        }
    }
    
    /**
     * 获取平均精度
     */
    private fun getAverageAccuracy(locations: List<LocationEntity>): String {
        val accuracies = locations.mapNotNull { it.accuracy }
        if (accuracies.isEmpty()) return "-"
        
        val avg = accuracies.average()
        return "${String.format("%.1f", avg)}米"
    }
    
    /**
     * 获取最高精度
     */
    private fun getMinAccuracy(locations: List<LocationEntity>): String {
        val accuracies = locations.mapNotNull { it.accuracy }
        if (accuracies.isEmpty()) return "-"
        
        val min = accuracies.minOrNull() ?: 0f
        return "${String.format("%.1f", min)}米"
    }
    
    /**
     * 获取最低精度
     */
    private fun getMaxAccuracy(locations: List<LocationEntity>): String {
        val accuracies = locations.mapNotNull { it.accuracy }
        if (accuracies.isEmpty()) return "-"
        
        val max = accuracies.maxOrNull() ?: 0f
        return "${String.format("%.1f", max)}米"
    }
    
    /**
     * 获取最大速度
     */
    private fun getMaxSpeed(locations: List<LocationEntity>): String {
        val speeds = locations.mapNotNull { it.speed }
        if (speeds.isEmpty()) return "-"
        
        val max = speeds.maxOrNull() ?: 0f
        return "${String.format("%.2f", max)}m/s"
    }
    
    /**
     * 获取平均速度
     */
    private fun getAverageSpeed(locations: List<LocationEntity>): String {
        val speeds = locations.mapNotNull { it.speed }
        if (speeds.isEmpty()) return "-"
        
        val avg = speeds.average()
        return "${String.format("%.2f", avg)}m/s"
    }
    
    /**
     * 获取静止记录占比
     */
    private fun getStationaryPercentage(locations: List<LocationEntity>): String {
        val speeds = locations.mapNotNull { it.speed }
        if (speeds.isEmpty()) return "0"
        
        val stationaryCount = speeds.count { it < 0.5f } // 速度小于0.5m/s认为是静止
        val percentage = (stationaryCount.toDouble() / speeds.size) * 100
        return String.format("%.1f", percentage)
    }
    
    /**
     * 获取最高海拔
     */
    private fun getMaxAltitude(locations: List<LocationEntity>): String {
        val altitudes = locations.mapNotNull { it.altitude }
        if (altitudes.isEmpty()) return "-"
        
        val max = altitudes.maxOrNull() ?: 0.0
        return "${String.format("%.1f", max)}米"
    }
}