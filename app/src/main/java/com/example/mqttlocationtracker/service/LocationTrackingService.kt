package com.example.mqttlocationtracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mqttlocationtracker.R

/**
 * 前台服务，用于在后台持续跟踪位置
 */
class LocationTrackingService : Service() {
    
    private val binder = LocalBinder()
    
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
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建前台服务通知
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("位置跟踪服务运行中")
            .setContentText("正在后台持续跟踪您的位置")
            .setSmallIcon(R.drawable.ic_notification) // 需要创建通知图标
            .setOngoing(true) // 设置为持续通知
            .build()
    }
    
    /**
     * 启动位置跟踪
     */
    fun startTracking() {
        Log.d(TAG, "Starting location tracking")
        // TODO: 实现位置跟踪逻辑
    }
    
    /**
     * 停止位置跟踪
     */
    fun stopTracking() {
        Log.d(TAG, "Stopping location tracking")
        // TODO: 停止位置跟踪逻辑
    }
    
    /**
     * 检查服务是否正在跟踪位置
     */
    fun isTracking(): Boolean {
        // TODO: 实现跟踪状态检查
        return false
    }
}