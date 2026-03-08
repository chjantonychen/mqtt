package com.example.mqttlocationtracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.mqttlocationtracker.R
import com.example.mqttlocationtracker.data.LocationData
import com.example.mqttlocationtracker.database.cleanup.DataCleanupManager
import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.example.mqttlocationtracker.database.repository.LocationRepository
import com.example.mqttlocationtracker.database.sync.SyncManager
import com.example.mqttlocationtracker.mqtt.MqttManager
import com.example.mqttlocationtracker.utils.Logger
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 前台服务，用于在后台持续跟踪位置
 */
class LocationTrackingService : Service() {
    
    private val binder = LocalBinder()
    private var isTracking = false
    
    // 位置服务相关
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    
    // MQTT客户端
    private lateinit var mqttManager: MqttManager
    private var mqttTopic: String = "location/tracker"
    
    // 数据库相关
    private lateinit var locationRepository: LocationRepository
    private lateinit var syncManager: SyncManager
    private lateinit var cleanupManager: DataCleanupManager
    
    // 网络状态监听
    private var isNetworkAvailable = false
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Logger.d(TAG, "Network available")
            isNetworkAvailable = true
            // 网络恢复时尝试同步未发送的数据
            syncUnsentData()
        }
        
        override fun onLost(network: Network) {
            super.onLost(network)
            Logger.d(TAG, "Network lost")
            isNetworkAvailable = false
        }
    }
    
    // 数据清理相关
    private var lastCleanupTime: Long = 0
    private val cleanupIntervalMillis = 24 * 60 * 60 * 1000L // 24小时
    
    // 协程作用域
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    // 服务状态
    private var isServiceInitialized = false
    
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
        Logger.d(TAG, "LocationTrackingService created")
        
        try {
            // 初始化位置服务
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            
            // 初始化MQTT管理器
            mqttManager = MqttManager(serviceScope)
            
            // 初始化数据库仓库
            locationRepository = LocationRepository(this)
            
            // 初始化同步管理器
            syncManager = SyncManager(locationRepository, mqttManager, serviceScope)
            
            // 初始化数据清理管理器
            cleanupManager = DataCleanupManager(locationRepository, serviceScope)
            
            // 注册网络状态监听器
            registerNetworkCallback()
            
            // 创建位置回调
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        handleLocationUpdate(location)
                    }
                }
                
                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    Logger.d(TAG, "Location availability: ${locationAvailability.isLocationAvailable}")
                }
            }
            
            // 创建通知频道
            createNotificationChannel()
            
            // 启动前台服务
            startForeground(NOTIFICATION_ID, createNotification())
            
            isServiceInitialized = true
            Logger.d(TAG, "LocationTrackingService initialized successfully")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize LocationTrackingService", e)
            stopSelf()
        }
    }
    
    override fun onBind(intent: Intent): IBinder {
        Logger.d(TAG, "LocationTrackingService bound")
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(TAG, "LocationTrackingService started with flags: $flags, startId: $startId")
        
        // 返回START_STICKY以确保服务在被杀死后能重启
        return START_STICKY
    }
    
    override fun onDestroy() {
        Logger.d(TAG, "LocationTrackingService destroying")
        
        // 注销网络状态监听器
        unregisterNetworkCallback()
        
        // 停止跟踪
        if (isTracking) {
            stopTracking()
        }
        
        // 断开MQTT连接
        serviceScope.launch {
            try {
                if (::mqttManager.isInitialized && mqttManager.isConnected()) {
                    mqttManager.disconnect()
                    Logger.d(TAG, "MQTT connection disconnected")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error disconnecting MQTT", e)
            }
        }
        
        // 取消协程作用域
        serviceScope.cancel()
        
        isServiceInitialized = false
        Logger.d(TAG, "LocationTrackingService destroyed")
        super.onDestroy()
    }
    
    /**
     * 注册网络状态监听器
     */
    private fun registerNetworkCallback() {
        try {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkRequest = NetworkRequest.Builder().build()
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            Logger.d(TAG, "Network callback registered")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to register network callback", e)
        }
    }
    
    /**
     * 注销网络状态监听器
     */
    private fun unregisterNetworkCallback() {
        try {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(networkCallback)
            Logger.d(TAG, "Network callback unregistered")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to unregister network callback", e)
        }
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
        if (isServiceInitialized) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        }
    }
    
    /**
     * 配置MQTT连接参数
     */
    fun configureMqtt(
        serverUri: String,
        clientId: String,
        username: String? = null,
        password: String? = null,
        useTls: Boolean = false,
        topic: String = "location/tracker"
    ) {
        if (!isServiceInitialized) {
            Logger.w(TAG, "Service not initialized, cannot configure MQTT")
            return
        }
        
        mqttManager.configure(serverUri, clientId, username, password, useTls)
        this.mqttTopic = topic
        Logger.d(TAG, "MQTT configured with server: $serverUri, topic: $topic")
    }
    
    /**
     * 连接到MQTT服务器
     */
    suspend fun connectToMqtt(): Boolean {
        if (!isServiceInitialized) {
            Logger.w(TAG, "Service not initialized, cannot connect to MQTT")
            return false
        }
        
        return try {
            mqttManager.connect()
            Logger.d(TAG, "MQTT connected successfully")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to connect to MQTT", e)
            false
        }
    }
    
    /**
     * 断开MQTT连接
     */
    suspend fun disconnectFromMqtt() {
        if (!isServiceInitialized) {
            Logger.w(TAG, "Service not initialized, cannot disconnect from MQTT")
            return
        }
        
        try {
            if (mqttManager.isConnected()) {
                mqttManager.disconnect()
                Logger.d(TAG, "MQTT disconnected successfully")
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error disconnecting from MQTT", e)
        }
    }
    
    /**
     * 启动位置跟踪
     */
    fun startTracking() {
        if (!isServiceInitialized) {
            Logger.w(TAG, "Service not initialized, cannot start tracking")
            return
        }
        
        if (isTracking) {
            Logger.d(TAG, "Already tracking, ignoring start request")
            return
        }
        
        Logger.d(TAG, "Starting location tracking")
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
            Logger.d(TAG, "Location updates requested successfully")
        } catch (e: SecurityException) {
            Logger.e(TAG, "Location permission not granted", e)
            isTracking = false
            updateNotification()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to start location tracking", e)
            isTracking = false
            updateNotification()
        }
    }
    
    /**
     * 停止位置跟踪
     */
    fun stopTracking() {
        if (!isServiceInitialized) {
            Logger.w(TAG, "Service not initialized, cannot stop tracking")
            return
        }
        
        if (!isTracking) {
            Logger.d(TAG, "Not tracking, ignoring stop request")
            return
        }
        
        Logger.d(TAG, "Stopping location tracking")
        isTracking = false
        updateNotification()
        
        try {
            // 移除位置更新请求
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Logger.d(TAG, "Location updates removed successfully")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to stop location tracking", e)
        }
    }
    
    /**
     * 处理位置更新
     */
    private fun handleLocationUpdate(location: Location) {
        if (!isServiceInitialized) {
            Logger.w(TAG, "Service not initialized, ignoring location update")
            return
        }
        
        Logger.d(TAG, "Location update: lat=${location.latitude}, lng=${location.longitude}, acc=${location.accuracy}")
        
        // 检查位置数据的有效性
        if (!isLocationValid(location)) {
            Logger.w(TAG, "Invalid location data received, ignoring")
            return
        }
        
        // 创建位置数据对象
        val locationData = LocationData(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            altitude = if (location.hasAltitude()) location.altitude else null,
            speed = if (location.hasSpeed()) location.speed else null,
            bearing = if (location.hasBearing()) location.bearing else null
        )
        
        // 保存位置数据到数据库
        saveLocationToDatabase(locationData)
        
        // 发布位置数据到MQTT
        publishLocationData(locationData)
        
        // 定期执行数据清理
        performPeriodicCleanup()
    }
    
    /**
     * 检查位置数据是否有效
     */
    private fun isLocationValid(location: Location): Boolean {
        // 检查基本有效性
        if (!location.hasAccuracy() || location.accuracy <= 0) {
            return false
        }
        
        // 检查经纬度是否有效
        if (location.latitude !in -90.0..90.0 || location.longitude !in -180.0..180.0) {
            return false
        }
        
        // 检查时间戳是否合理（不能是未来时间）
        if (location.time > System.currentTimeMillis() + 10000) { // 允许10秒误差
            return false
        }
        
        return true
    }
    
    /**
     * 保存位置数据到数据库
     */
    private fun saveLocationToDatabase(locationData: LocationData) {
        serviceScope.launch {
            try {
                val locationEntity = LocationEntity.fromLocationData(locationData)
                locationRepository.insertLocation(locationEntity)
                Logger.d(TAG, "Location data saved to database")
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to save location data to database", e)
            }
        }
    }
    
    /**
     * 发布位置数据到MQTT
     */
    private fun publishLocationData(locationData: LocationData) {
        if (!isServiceInitialized) {
            Logger.w(TAG, "Service not initialized, cannot publish location data")
            return
        }
        
        serviceScope.launch {
            try {
                if (mqttManager.isConnected()) {
                    mqttManager.publish(mqttTopic, locationData.toJson())
                    Logger.d(TAG, "Location data published to MQTT topic: $mqttTopic")
                } else {
                    Logger.w(TAG, "MQTT not connected, location saved for later sync")
                    // 网络不可用时，位置数据已经在数据库中保存
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to publish location data to MQTT", e)
                // 发布失败，位置数据已在数据库中保存，等待后续同步
            }
        }
    }
    
    /**
     * 同步未发送的数据
     */
    private fun syncUnsentData() {
        Logger.d(TAG, "Attempting to sync unsent data")
        syncManager.syncUnsentLocations(mqttTopic)
    }
    
    /**
     * 执行定期数据清理
     */
    private fun performPeriodicCleanup() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCleanupTime > cleanupIntervalMillis) {
            Logger.d(TAG, "Performing periodic data cleanup")
            cleanupManager.cleanupExpiredData()
            lastCleanupTime = currentTime
        }
    }
    
    /**
     * 检查服务是否正在跟踪位置
     */
    fun isTracking(): Boolean {
        return isTracking
    }
    
    /**
     * 检查MQTT是否已连接
     */
    fun isMqttConnected(): Boolean {
        return ::mqttManager.isInitialized && mqttManager.isConnected()
    }
    
    /**
     * 检查服务是否已初始化
     */
    fun isServiceInitialized(): Boolean {
        return isServiceInitialized
    }
    
    /**
     * 获取位置数据仓库
     */
    fun getLocationRepository(): LocationRepository {
        return locationRepository
    }
}