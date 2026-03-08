package com.example.mqttlocationtracker.utils

import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.heatmaps.WeightedLatLng
import kotlin.math.*

/**
 * 热力图助手类，用于生成位置数据的热力图
 */
object HeatmapHelper {
    
    private const val TAG = "HeatmapHelper"
    
    /**
     * 生成加权位置点列表用于热力图
     *
     * @param locations 位置数据列表
     * @param intensityMode 强度模式
     * @return 加权位置点列表
     */
    fun generateWeightedLocations(
        locations: List<LocationEntity>,
        intensityMode: IntensityMode = IntensityMode.FREQUENCY
    ): List<WeightedLatLng> {
        return try {
            when (intensityMode) {
                IntensityMode.FREQUENCY -> generateFrequencyBasedWeights(locations)
                IntensityMode.ACCURACY -> generateAccuracyBasedWeights(locations)
                IntensityMode.SPEED -> generateSpeedBasedWeights(locations)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate weighted locations for heatmap", e)
            emptyList()
        }
    }
    
    /**
     * 基于频率的权重生成（访问次数越多，权重越高）
     */
    private fun generateFrequencyBasedWeights(locations: List<LocationEntity>): List<WeightedLatLng> {
        // 按地理位置分组
        val locationGroups = mutableMapOf<String, MutableList<LocationEntity>>()
        
        for (location in locations) {
            // 使用经纬度创建键（精度到小数点后5位）
            val key = "%.5f,%.5f".format(location.latitude, location.longitude)
            if (!locationGroups.containsKey(key)) {
                locationGroups[key] = mutableListOf()
            }
            locationGroups[key]?.add(location)
        }
        
        // 为每个分组创建加权位置点
        val weightedLocations = mutableListOf<WeightedLatLng>()
        for ((_, group) in locationGroups) {
            val centerLat = group.sumOf { it.latitude } / group.size
            val centerLng = group.sumOf { it.longitude } / group.size
            val weight = group.size.toDouble() // 权重等于该位置的访问次数
            
            weightedLocations.add(
                WeightedLatLng(
                    LatLng(centerLat, centerLng),
                    weight
                )
            )
        }
        
        return weightedLocations
    }
    
    /**
     * 基于精度的权重生成（精度越高，权重越高）
     */
    private fun generateAccuracyBasedWeights(locations: List<LocationEntity>): List<WeightedLatLng> {
        val weightedLocations = mutableListOf<WeightedLatLng>()
        
        for (location in locations) {
            // 精度值越小表示越精确，所以我们使用 1/accuracy 作为权重
            // 如果没有精度信息，则使用默认权重
            val weight = location.accuracy?.let { acc ->
                if (acc > 0) 1.0 / acc else 1.0
            } ?: 1.0
            
            weightedLocations.add(
                WeightedLatLng(
                    LatLng(location.latitude, location.longitude),
                    weight
                )
            )
        }
        
        return weightedLocations
    }
    
    /**
     * 基于速度的权重生成（速度越快，权重越高）
     */
    private fun generateSpeedBasedWeights(locations: List<LocationEntity>): List<WeightedLatLng> {
        val weightedLocations = mutableListOf<WeightedLatLng>()
        
        for (location in locations) {
            // 使用速度作为权重，如果没有速度信息则使用默认权重
            val weight = location.speed?.toDouble() ?: 1.0
            
            weightedLocations.add(
                WeightedLatLng(
                    LatLng(location.latitude, location.longitude),
                    weight
                )
            )
        }
        
        return weightedLocations
    }
    
    /**
     * 计算合适的热力图半径
     *
     * @param zoomLevel 当前缩放级别
     * @return 热力图半径（像素）
     */
    fun calculateHeatmapRadius(zoomLevel: Float): Double {
        // 根据缩放级别动态调整半径
        // 缩放级别越高，半径越小；缩放级别越低，半径越大
        return when {
            zoomLevel >= 15 -> 20.0
            zoomLevel >= 12 -> 30.0
            zoomLevel >= 8 -> 50.0
            else -> 100.0
        }
    }
    
    /**
     * 计算合适的热力图透明度
     *
     * @param zoomLevel 当前缩放级别
     * @return 热力图透明度 (0.0 - 1.0)
     */
    fun calculateHeatmapOpacity(zoomLevel: Float): Double {
        // 根据缩放级别调整透明度
        return when {
            zoomLevel >= 15 -> 0.9
            zoomLevel >= 12 -> 0.8
            zoomLevel >= 8 -> 0.7
            else -> 0.6
        }
    }
    
    /**
     * 强度模式枚举
     */
    enum class IntensityMode {
        FREQUENCY,  // 基于访问频率
        ACCURACY,   // 基于位置精度
        SPEED       // 基于移动速度
    }
}