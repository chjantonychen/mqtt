package com.example.mqttlocationtracker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mqttlocationtracker.mqtt.MqttManager
import com.example.mqttlocationtracker.service.LocationTrackingService
import com.example.mqttlocationtracker.ui.main.MainViewModel
import com.example.mqttlocationtracker.ui.main.MainViewModelFactory
import com.example.mqttlocationtracker.ui.settings.SettingsManager
import com.example.mqttlocationtracker.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private lateinit var mqttManager: MqttManager
    private var locationService: LocationTrackingService? = null
    private lateinit var permissionManager: PermissionManager
    private lateinit var settingsManager: SettingsManager
    
    // 服务连接
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationTrackingService.LocalBinder
            locationService = binder.getService()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            locationService = null
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化MQTT管理器
        mqttManager = MqttManager(lifecycleScope)
        
        // 初始化权限管理器
        permissionManager = PermissionManager(this)
        
        // 初始化设置管理器
        settingsManager = SettingsManager.getInstance(this)
        
        // 观察权限状态变化
        permissionManager.observePermissionStatus(this) { status ->
            when (status) {
                PermissionManager.PermissionStatus.GRANTED -> {
                    // 权限已授予，可以开始位置跟踪
                    bindLocationService()
                }
                PermissionManager.PermissionStatus.DENIED -> {
                    // 权限被拒绝，显示提示信息
                }
                PermissionManager.PermissionStatus.DENIED_FOREVER -> {
                    // 权限被永久拒绝，引导用户到设置页面
                }
            }
        }
        
        setContent {
            MainApp(
                mqttManager = mqttManager,
                permissionManager = permissionManager,
                settingsManager = settingsManager,
                locationService = locationService
            )
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 检查并请求位置权限
        permissionManager.checkAndRequestLocationPermissions()
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 处理权限请求结果
        if (!permissionManager.handlePermissionsResult(requestCode, grantResults)) {
            // 如果不是权限管理器处理的请求，调用父类处理
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 解绑服务
        try {
            unbindService(serviceConnection)
        } catch (e: IllegalArgumentException) {
            // 服务未绑定，忽略异常
        }
    }
    
    /**
     * 绑定位置跟踪服务
     */
    private fun bindLocationService() {
        val intent = Intent(this, LocationTrackingService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    /**
     * 启动位置跟踪服务
     */
    private fun startLocationTrackingService() {
        val intent = Intent(this, LocationTrackingService::class.java)
        startService(intent)
    }
}

@Composable
fun MainApp(
    mqttManager: MqttManager,
    permissionManager: PermissionManager,
    settingsManager: SettingsManager,
    locationService: LocationTrackingService?
) {
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(mqttManager, settingsManager)
    )
    
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    
    when (currentScreen) {
        Screen.Main -> MainScreen(
            viewModel = viewModel,
            permissionManager = permissionManager,
            locationService = locationService,
            onNavigateToSettings = { currentScreen = Screen.Settings }
        )
        Screen.Settings -> SettingsScreen(
            settingsManager = settingsManager,
            onNavigateBack = { currentScreen = Screen.Main }
        )
    }
}

sealed class Screen {
    object Main : Screen()
    object Settings : Screen()
}