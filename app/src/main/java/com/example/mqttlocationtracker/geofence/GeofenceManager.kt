package com.example.mqttlocationtracker.geofence

import android.content.Context
import android.content.Intent
import android.location.Location
import com.example.mqttlocationtracker.data.LocationData
import com.example.mqttlocationtracker.utils.Logger
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlin.math.*

/**
 * 地理围栏管理器，用于监控用户进出特定区域
 */
class GeofenceManager(private val context: Context) {
    
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val geofences = mutableMapOf<String, GeofenceData>()
    
    companion object {
        private const val TAG = "GeofenceManager"
        private const val GEOFENCE_RADIUS_DEFAULT = 100.0 // 默认围栏半径（米）
    }
    
    /**
     * 地理围栏数据类
     */
    data class GeofenceData(
        val id: String,
        val latitude: Double,
        val longitude: Double,
        val radius: Float,
        val name: String,
        val description: String = "",
        val transitionTypes: Int = Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT,
        val expirationDuration: Long = Geofence.NEVER_EXPIRE,
        val notificationResponsiveness: Int = 0 // 0表示默认响应性
    )
    
    /**
     * 添加地理围栏
     */
    fun addGeofence(geofenceData: GeofenceData): Boolean {
        return try {
            val geofence = Geofence.Builder()
                .setRequestId(geofenceData.id)
                .setCircularRegion(
                    geofenceData.latitude,
                    geofenceData.longitude,
                    geofenceData.radius
                )
                .setTransitionTypes(geofenceData.transitionTypes)
                .setExpirationDuration(geofenceData.expirationDuration)
                .setNotificationResponsiveness(geofenceData.notificationResponsiveness)
                .build()
            
            geofences[geofenceData.id] = geofenceData
            
            Logger.d(TAG, "Geofence added: ${geofenceData.name} (${geofenceData.id})")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to add geofence: ${geofenceData.name}", e)
            false
        }
    }
    
    /**
     * 移除地理围栏
     */
    fun removeGeofence(geofenceId: String): Boolean {
        return try {
            geofences.remove(geofenceId)
            Logger.d(TAG, "Geofence removed: $geofenceId")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to remove geofence: $geofenceId", e)
            false
        }
    }
    
    /**
     * 移除所有地理围栏
     */
    fun removeAllGeofences(): Boolean {
        return try {
            geofences.clear()
            Logger.d(TAG, "All geofences removed")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to remove all geofences", e)
            false
        }
    }
    
    /**
     * 检查位置是否在地理围栏内
     */
    fun isLocationInGeofence(geofenceId: String, location: LocationData): Boolean {
        val geofenceData = geofences[geofenceId] ?: return false
        return isLocationInRegion(
            location.latitude,
            location.longitude,
            geofenceData.latitude,
            geofenceData.longitude,
            geofenceData.radius.toDouble()
        )
    }
    
    /**
     * 检查位置是否在区域内
     */
    private fun isLocationInRegion(
        lat: Double,
        lng: Double,
        centerLat: Double,
        centerLng: Double,
        radius: Double
    ): Boolean {
        val distance = calculateDistance(lat, lng, centerLat, centerLng)
        return distance <= radius
    }
    
    /**
     * 计算两个位置之间的距离（米）
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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
     * 获取所有地理围栏
     */
    fun getAllGeofences(): List<GeofenceData> {
        return geofences.values.toList()
    }
    
    /**
     * 根据位置获取触发的地理围栏
     */
    fun getTriggeredGeofences(location: LocationData): List<GeofenceData> {
        return geofences.values.filter { geofence ->
            isLocationInGeofence(geofence.id, location)
        }
    }
    
    /**
     * 创建地理围栏请求
     */
    private fun createGeofencingRequest(geofenceId: String): GeofencingRequest? {
        val geofenceData = geofences[geofenceId] ?: return null
        
        val geofence = Geofence.Builder()
            .setRequestId(geofenceData.id)
            .setCircularRegion(
                geofenceData.latitude,
                geofenceData.longitude,
                geofenceData.radius
            )
            .setTransitionTypes(geofenceData.transitionTypes)
            .setExpirationDuration(geofenceData.expirationDuration)
            .setNotificationResponsiveness(geofenceData.notificationResponsiveness)
            .build()
        
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }
    
    /**
     * 获取地理围栏客户端
     */
    fun getGeofencingClient(): GeofencingClient {
        return geofencingClient
    }
    
    /**
     * 检查地理围栏权限
     */
    fun hasGeofencePermissions(): Boolean {
        // 在实际应用中，这里需要检查相应的权限
        return true
    }
}