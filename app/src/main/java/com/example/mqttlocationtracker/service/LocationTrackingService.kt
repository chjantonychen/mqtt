package com.example.mqttlocationtracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mqttlocationtracker.R
import com.google.android.gms.location.*

/**
 * 前台服务，用于在后台持续跟踪位置
 */
class LocationTrackingService : Service() {
    
    private val binder = LocalBinder()
    private var isTracking = false
    
    // 位置服务相关
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    
    // 通知ID和频道ID
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "location_tracking_channel"
        const val CHANNEL_NAME = "位置跟踪服务"
        const val TAG = "LocationTrackingService"
    }
    
    /**
     * Binder类，用于Activity与Service通信
     */
    inner class LocalBinder : Binder() {
        fun getService(): LocationTrackingService = this@LocationTrackingService
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "LocationTrackingService created")
        
        // 初始化位置服务
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // 创建位置回调
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    handleLocationUpdate(location)
                }
            }
            
            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                Log.d(TAG, "Location availability: ${locationAvailability.isLocationAvailable}")
            }
        }
        
        // 创建通知频道
        createNotificationChannel()
        
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "LocationTrackingService bound")
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "LocationTrackingService started with flags: $flags, startId: $startId")
        
        // 返回START_STICKY以确保服务在被杀死后能重启
        return START_STICKY
    }
    
    override fun onDestroy() {
        Log.d(TAG, "LocationTrackingService destroyed")
        stopTracking()
        super.onDestroy()
    }
    
    /**
     * 创建通知频道（Android 8.0及以上版本需要）
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于在后台持续跟踪位置的服务通知"
                setShowBadge(false) // 不显示徽章
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建前台服务通知
     */
    private fun createNotification(): Notification {
        val title = if (isTracking) "位置跟踪进行中" else "位置跟踪服务运行中"
        val content = if (isTracking) "正在持续跟踪您的位置" else "服务运行中，点击开始跟踪"
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true) // 设置为持续通知
            .setShowWhen(false) // 不显示时间戳
            .build()
    }
    
    /**
     * 更新通知
     */
    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }
    
    /**
     * 启动位置跟踪
     */
    fun startTracking() {
        if (isTracking) {
            Log.d(TAG, "Already tracking, ignoring start request")
            return
        }
        
        Log.d(TAG, "Starting location tracking")
        isTracking = true
        updateNotification()
        
        // 创建位置请求
        val locationRequest = LocationRequest.Builder(1000L) // 1秒间隔
            .setMinUpdateIntervalMillis(500L) // 最小0.5秒间隔
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateDelayMillis(2000L) // 最大延迟2秒
            .build()
        
        try {
            // 请求位置更新
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Location updates requested successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted", e)
            isTracking = false
            updateNotification()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start location tracking", e)
            isTracking = false
            updateNotification()
        }
    }
    
    /**
     * 停止位置跟踪
     */
    fun stopTracking() {
        if (!isTracking) {
            Log.d(TAG, "Not tracking, ignoring stop request")
            return
        }
        
        Log.d(TAG, "Stopping location tracking")
        isTracking = false
        updateNotification()
        
        try {
            // 移除位置更新请求
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.d(TAG, "Location updates removed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop location tracking", e)
        }
    }
    
    /**
     * 处理位置更新
     */
    private fun handleLocationUpdate(location: Location) {
        Log.d(TAG, "Location update: lat=${location.latitude}, lng=${location.longitude}, acc=${location.accuracy}")
        // TODO: 处理位置更新，例如发送到MQTT服务器
    }
    
    /**
     * 检查服务是否正在跟踪位置
     */
    fun isTracking(): Boolean {
        return isTracking
    }
}