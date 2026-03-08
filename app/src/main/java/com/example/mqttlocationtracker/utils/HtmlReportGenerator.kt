package com.example.mqttlocationtracker.utils

import android.content.Context
import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * HTML报告生成器，用于将位置数据导出为交互式HTML报告
 */
object HtmlReportGenerator {
    
    private const val TAG = "HtmlReportGenerator"
    
    /**
     * 生成位置数据HTML报告
     */
    fun generateLocationReportHtml(
        context: Context,
        locations: List<LocationEntity>,
        outputStream: FileOutputStream,
        title: String = "位置跟踪报告"
    ): Boolean {
        return try {
            val htmlContent = buildHtmlReport(context, locations, title)
            outputStream.write(htmlContent.toByteArray())
            outputStream.flush()
            outputStream.close()
            
            Logger.i(TAG, "HTML report generated successfully with ${locations.size} locations")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate HTML report", e)
            false
        }
    }
    
    /**
     * 构建HTML报告
     */
    private fun buildHtmlReport(
        context: Context,
        locations: List<LocationEntity>,
        title: String
    ): String {
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$title</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            border-radius: 10px;
            margin-bottom: 30px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        
        .header h1 {
            margin: 0 0 10px 0;
            font-size: 2.5em;
        }
        
        .header p {
            margin: 5px 0;
            font-size: 1.1em;
            opacity: 0.9;
        }
        
        .stats-container {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .stat-card {
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            text-align: center;
        }
        
        .stat-card h3 {
            margin: 0 0 10px 0;
            color: #667eea;
        }
        
        .stat-card .value {
            font-size: 2em;
            font-weight: bold;
            color: #333;
        }
        
        .chart-container {
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 30px;
        }
        
        .chart-container h2 {
            margin-top: 0;
            color: #333;
        }
        
        .map-container {
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 30px;
        }
        
        .map-placeholder {
            height: 400px;
            background: #e9ecef;
            border-radius: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #6c757d;
            font-size: 1.2em;
        }
        
        .data-table {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            overflow: hidden;
            margin-bottom: 30px;
        }
        
        .data-table h2 {
            margin: 0;
            padding: 20px;
            background: #667eea;
            color: white;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
        }
        
        th, td {
            padding: 12px 15px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        
        th {
            background-color: #f8f9fa;
            font-weight: bold;
        }
        
        tr:nth-child(even) {
            background-color: #f8f9fa;
        }
        
        tr:hover {
            background-color: #e9ecef;
        }
        
        .footer {
            text-align: center;
            padding: 20px;
            color: #6c757d;
            font-size: 0.9em;
        }
        
        @media (max-width: 768px) {
            .stats-container {
                grid-template-columns: 1fr;
            }
            
            body {
                padding: 10px;
            }
            
            .header {
                padding: 20px;
            }
            
            .header h1 {
                font-size: 2em;
            }
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>$title</h1>
        <p>生成时间: ${SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault()).format(Date())}</p>
        <p>MQTT位置跟踪器 - 专业的位置数据管理工具</p>
    </div>
    
    <div class="stats-container">
        <div class="stat-card">
            <h3>总位置点</h3>
            <div class="value">${locations.size}</div>
        </div>
        
        <div class="stat-card">
            <h3>数据范围</h3>
            <div class="value">${getDateRangeDays(locations)}天</div>
        </div>
        
        <div class="stat-card">
            <h3>平均精度</h3>
            <div class="value">${getAverageAccuracy(locations)}</div>
        </div>
        
        <div class="stat-card">
            <h3>最高速度</h3>
            <div class="value">${getMaxSpeed(locations)}</div>
        </div>
    </div>
    
    <div class="chart-container">
        <h2>📊 数据统计图表</h2>
        <p>此处将显示位置数据的统计图表（在完整实现中）</p>
        <div style="height: 300px; background: #e9ecef; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #6c757d;">
            数据统计图表占位符
        </div>
    </div>
    
    <div class="map-container">
        <h2>🗺️ 位置轨迹图</h2>
        <p>以下是您的位置轨迹可视化展示：</p>
        <div class="map-placeholder">
            位置轨迹图占位符（在完整实现中将显示交互式地图）
        </div>
    </div>
    
    <div class="data-table">
        <h2>📋 详细位置数据</h2>
        <table>
            <thead>
                <tr>
                    <th>时间</th>
                    <th>纬度</th>
                    <th>经度</th>
                    <th>精度(米)</th>
                    <th>海拔(米)</th>
                    <th>速度(m/s)</th>
                    <th>状态</th>
                </tr>
            </thead>
            <tbody>
                ${generateTableRows(locations)}
            </tbody>
        </table>
    </div>
    
    <div class="footer">
        <p>本报告由MQTT位置跟踪器自动生成</p>
        <p>© ${SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())} MQTT位置跟踪器. 保留所有权利.</p>
    </div>
</body>
</html>
        """.trimIndent()
    }
    
    /**
     * 生成表格行
     */
    private fun generateTableRows(locations: List<LocationEntity>): String {
        val rows = StringBuilder()
        
        // 只显示最近的100条记录，避免HTML过大
        val displayLocations = if (locations.size > 100) {
            locations.sortedByDescending { it.timestamp }.take(100)
        } else {
            locations
        }
        
        for (location in displayLocations) {
            rows.append("""
                <tr>
                    <td>${SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(location.timestamp))}</td>
                    <td>${String.format("%.6f", location.latitude)}</td>
                    <td>${String.format("%.6f", location.longitude)}</td>
                    <td>${location.accuracy?.let { String.format("%.1f", it) } ?: "-"}</td>
                    <td>${location.altitude?.let { String.format("%.1f", it) } ?: "-"}</td>
                    <td>${location.speed?.let { String.format("%.2f", it) } ?: "-"}</td>
                    <td>${if (location.isSynced) "已同步" else "未同步"}</td>
                </tr>
            """.trimIndent())
        }
        
        // 如果有更多数据，添加提示
        if (locations.size > 100) {
            rows.append("""
                <tr>
                    <td colspan="7" style="text-align: center; font-style: italic;">
                        仅显示最近100条记录，总共${locations.size}条记录
                    </td>
                </tr>
            """.trimIndent())
        }
        
        return rows.toString()
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
        if (accuracies.isEmpty()) return "-"
        
        val avg = accuracies.average()
        return "${String.format("%.1f", avg)}米"
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
}