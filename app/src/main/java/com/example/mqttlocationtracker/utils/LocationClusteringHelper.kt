package com.example.mqttlocationtracker.utils

import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

/**
 * 位置数据聚类助手类，用于在地图上对大量位置点进行聚类以提高性能
 */
object LocationClusteringHelper {
    
    private const val TAG = "LocationClusteringHelper"
    
    /**
     * 聚类算法参数
     */
    private const val DEFAULT_GRID_SIZE = 100.0 // 网格大小（像素）
    private const val DEFAULT_MIN_CLUSTER_SIZE = 2 // 最小聚类大小
    
    /**
     * 对位置数据进行聚类
     *
     * @param locations 位置数据列表
     * @param zoomLevel 当前缩放级别
     * @param screenWidth 屏幕宽度（像素）
     * @param screenHeight 屏幕高度（像素）
     * @return 聚类结果
     */
    fun clusterLocations(
        locations: List<LocationEntity>,
        zoomLevel: Float,
        screenWidth: Int,
        screenHeight: Int
    ): List<Cluster> {
        return try {
            if (locations.isEmpty()) {
                return emptyList()
            }
            
            // 计算网格大小（根据缩放级别调整）
            val gridSize = calculateGridSize(zoomLevel)
            
            // 创建网格
            val grid = createGrid(locations, gridSize)
            
            // 生成聚类
            val clusters = generateClusters(grid, gridSize)
            
            Logger.d(TAG, "Clustering completed: ${locations.size} locations -> ${clusters.size} clusters")
            clusters
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to cluster locations", e)
            // 出错时返回原始位置点
            locations.map { location ->
                Cluster(
                    position = LatLng(location.latitude, location.longitude),
                    locations = listOf(location),
                    isCluster = false
                )
            }
        }
    }
    
    /**
     * 根据缩放级别计算网格大小
     */
    private fun calculateGridSize(zoomLevel: Float): Double {
        // 缩放级别越高，网格越小（更精细的聚类）
        // 缩放级别越低，网格越大（更粗略的聚类）
        return DEFAULT_GRID_SIZE / (2.0.pow(zoomLevel / 2.0))
    }
    
    /**
     * 创建网格
     */
    private fun createGrid(locations: List<LocationEntity>, gridSize: Double): Map<GridKey, MutableList<LocationEntity>> {
        val grid = mutableMapOf<GridKey, MutableList<LocationEntity>>()
        
        for (location in locations) {
            val gridKey = GridKey(
                x = (location.longitude / gridSize).toInt(),
                y = (location.latitude / gridSize).toInt()
            )
            
            if (!grid.containsKey(gridKey)) {
                grid[gridKey] = mutableListOf()
            }
            grid[gridKey]?.add(location)
        }
        
        return grid
    }
    
    /**
     * 生成聚类
     */
    private fun generateClusters(grid: Map<GridKey, MutableList<LocationEntity>>, gridSize: Double): List<Cluster> {
        val clusters = mutableListOf<Cluster>()
        
        for ((_, locationsInCell) in grid) {
            if (locationsInCell.size >= DEFAULT_MIN_CLUSTER_SIZE) {
                // 创建聚类
                val center = calculateCenter(locationsInCell)
                clusters.add(
                    Cluster(
                        position = center,
                        locations = locationsInCell,
                        isCluster = true
                    )
                )
            } else {
                // 单独的点
                for (location in locationsInCell) {
                    clusters.add(
                        Cluster(
                            position = LatLng(location.latitude, location.longitude),
                            locations = listOf(location),
                            isCluster = false
                        )
                    )
                }
            }
        }
        
        return clusters
    }
    
    /**
     * 计算位置列表的中心点
     */
    private fun calculateCenter(locations: List<LocationEntity>): LatLng {
        var latSum = 0.0
        var lngSum = 0.0
        
        for (location in locations) {
            latSum += location.latitude
            lngSum += location.longitude
        }
        
        return LatLng(
            latSum / locations.size,
            lngSum / locations.size
        )
    }
    
    /**
     * 计算两个位置之间的距离（米）
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // 地球半径（米）
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
    
    /**
     * 网格键数据类
     */
    data class GridKey(val x: Int, val y: Int)
    
    /**
     * 聚类数据类
     */
    data class Cluster(
        val position: LatLng,
        val locations: List<LocationEntity>,
        val isCluster: Boolean
    ) {
        /**
         * 获取聚类标题
         */
        val title: String
            get() = if (isCluster) {
                "${locations.size} 个位置点"
            } else {
                "位置点"
            }
        
        /**
         * 获取聚类描述
         */
        val description: String
            get() = if (isCluster) {
                "包含 ${locations.size} 个位置点"
            } else {
                val location = locations.firstOrNull()
                location?.let {
                    "时间: ${java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it.timestamp))}"
                } ?: ""
            }
    }
}