package com.example.mqttlocationtracker.utils

import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/**
 * 图表助手类，用于生成位置数据的统计图表
 */
object ChartHelper {
    
    private const val TAG = "ChartHelper"
    
    /**
     * 生成每日位置数量数据
     */
    fun generateDailyLocationCounts(locations: List<LocationEntity>): List<ChartDataPoint> {
        return try {
            if (locations.isEmpty()) {
                return emptyList()
            }
            
            // 按日期分组
            val dailyCounts = mutableMapOf<String, Int>()
            
            for (location in locations) {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(location.timestamp))
                dailyCounts[dateStr] = dailyCounts.getOrDefault(dateStr, 0) + 1
            }
            
            // 转换为图表数据点
            val chartData = dailyCounts.map { (date, count) ->
                ChartDataPoint(
                    label = date,
                    value = count.toDouble(),
                    timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)?.time ?: 0
                )
            }.sortedBy { it.timestamp }
            
            Logger.d(TAG, "Generated ${chartData.size} daily location count data points")
            chartData
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate daily location counts", e)
            emptyList()
        }
    }
    
    /**
     * 生成每小时位置数量数据
     */
    fun generateHourlyLocationCounts(locations: List<LocationEntity>): List<ChartDataPoint> {
        return try {
            if (locations.isEmpty()) {
                return emptyList()
            }
            
            // 按小时分组
            val hourlyCounts = mutableMapOf<String, Int>()
            
            for (location in locations) {
                val hourStr = SimpleDateFormat("HH", Locale.getDefault()).format(Date(location.timestamp))
                hourlyCounts[hourStr] = hourlyCounts.getOrDefault(hourStr, 0) + 1
            }
            
            // 转换为图表数据点（按小时排序）
            val chartData = hourlyCounts.map { (hour, count) ->
                ChartDataPoint(
                    label = "${hour}点",
                    value = count.toDouble(),
                    timestamp = hour.toLong()
                )
            }.sortedBy { it.timestamp }
            
            Logger.d(TAG, "Generated ${chartData.size} hourly location count data points")
            chartData
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate hourly location counts", e)
            emptyList()
        }
    }
    
    /**
     * 生成精度分布数据
     */
    fun generateAccuracyDistribution(locations: List<LocationEntity>): List<ChartDataPoint> {
        return try {
            if (locations.isEmpty()) {
                return emptyList()
            }
            
            // 过滤有精度数据的位置
            val locationsWithAccuracy = locations.filter { it.accuracy != null }
            
            if (locationsWithAccuracy.isEmpty()) {
                return emptyList()
            }
            
            // 定义精度区间
            val ranges = listOf(
                0.0 to 10.0,
                10.0 to 30.0,
                30.0 to 50.0,
                50.0 to 100.0,
                100.0 to 200.0,
                200.0 to Double.MAX_VALUE
            )
            
            val distribution = mutableMapOf<String, Int>()
            
            for (location in locationsWithAccuracy) {
                val accuracy = location.accuracy!!
                val rangeLabel = when {
                    accuracy < 10.0 -> "<10m"
                    accuracy < 30.0 -> "10-30m"
                    accuracy < 50.0 -> "30-50m"
                    accuracy < 100.0 -> "50-100m"
                    accuracy < 200.0 -> "100-200m"
                    else -> ">200m"
                }
                distribution[rangeLabel] = distribution.getOrDefault(rangeLabel, 0) + 1
            }
            
            // 转换为图表数据点
            val chartData = distribution.map { (range, count) ->
                ChartDataPoint(
                    label = range,
                    value = count.toDouble()
                )
            }
            
            Logger.d(TAG, "Generated ${chartData.size} accuracy distribution data points")
            chartData
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate accuracy distribution", e)
            emptyList()
        }
    }
    
    /**
     * 生成速度分布数据
     */
    fun generateSpeedDistribution(locations: List<LocationEntity>): List<ChartDataPoint> {
        return try {
            if (locations.isEmpty()) {
                return emptyList()
            }
            
            // 过滤有速度数据的位置
            val locationsWithSpeed = locations.filter { it.speed != null }
            
            if (locationsWithSpeed.isEmpty()) {
                return emptyList()
            }
            
            // 定义速度区间（m/s）
            val ranges = listOf(
                0.0 to 1.0,
                1.0 to 3.0,
                3.0 to 5.0,
                5.0 to 10.0,
                10.0 to 20.0,
                20.0 to Double.MAX_VALUE
            )
            
            val distribution = mutableMapOf<String, Int>()
            
            for (location in locationsWithSpeed) {
                val speed = location.speed!!
                val rangeLabel = when {
                    speed < 1.0 -> "<1m/s"
                    speed < 3.0 -> "1-3m/s"
                    speed < 5.0 -> "3-5m/s"
                    speed < 10.0 -> "5-10m/s"
                    speed < 20.0 -> "10-20m/s"
                    else -> ">20m/s"
                }
                distribution[rangeLabel] = distribution.getOrDefault(rangeLabel, 0) + 1
            }
            
            // 转换为图表数据点
            val chartData = distribution.map { (range, count) ->
                ChartDataPoint(
                    label = range,
                    value = count.toDouble()
                )
            }
            
            Logger.d(TAG, "Generated ${chartData.size} speed distribution data points")
            chartData
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate speed distribution", e)
            emptyList()
        }
    }
    
    /**
     * 计算基本统计数据
     */
    fun calculateBasicStatistics(locations: List<LocationEntity>): BasicStatistics {
        if (locations.isEmpty()) {
            return BasicStatistics()
        }
        
        return try {
            val timestamps = locations.map { it.timestamp }
            val accuracies = locations.mapNotNull { it.accuracy }
            val speeds = locations.mapNotNull { it.speed }
            val altitudes = locations.mapNotNull { it.altitude }
            
            BasicStatistics(
                totalLocations = locations.size,
                dateRangeStart = timestamps.minOrNull() ?: 0,
                dateRangeEnd = timestamps.maxOrNull() ?: 0,
                averageAccuracy = if (accuracies.isNotEmpty()) accuracies.average() else null,
                averageSpeed = if (speeds.isNotEmpty()) speeds.average() else null,
                averageAltitude = if (altitudes.isNotEmpty()) altitudes.average() else null,
                maxAccuracy = accuracies.maxOrNull(),
                minAccuracy = accuracies.minOrNull(),
                maxSpeed = speeds.maxOrNull(),
                minSpeed = speeds.minOrNull()
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to calculate basic statistics", e)
            BasicStatistics()
        }
    }
    
    /**
     * 图表数据点
     */
    data class ChartDataPoint(
        val label: String,
        val value: Double,
        val timestamp: Long = 0
    )
    
    /**
     * 基本统计数据
     */
    data class BasicStatistics(
        val totalLocations: Int = 0,
        val dateRangeStart: Long = 0,
        val dateRangeEnd: Long = 0,
        val averageAccuracy: Double? = null,
        val averageSpeed: Double? = null,
        val averageAltitude: Double? = null,
        val maxAccuracy: Float? = null,
        val minAccuracy: Float? = null,
        val maxSpeed: Float? = null,
        val minSpeed: Float? = null
    ) {
        /**
         * 获取数据范围天数
         */
        val dateRangeDays: Int
            get() = if (dateRangeStart > 0 && dateRangeEnd > 0) {
                ((dateRangeEnd - dateRangeStart) / (1000 * 60 * 60 * 24)).toInt()
            } else {
                0
            }
        
        /**
         * 格式化日期范围
         */
        fun formatDateRange(): String {
            return if (dateRangeStart > 0 && dateRangeEnd > 0) {
                val startStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateRangeStart))
                val endStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateRangeEnd))
                "$startStr 至 $endStr"
            } else {
                "无数据"
            }
        }
    }
}