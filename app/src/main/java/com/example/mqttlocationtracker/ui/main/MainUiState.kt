package com.example.mqttlocationtracker.ui.main

import androidx.compose.runtime.Immutable
import com.example.mqttlocationtracker.data.LocationData

/**
 * 主界面状态数据类
 */
@Immutable
data class MainUiState(
    val isConnected: Boolean = false,
    val isTracking: Boolean = false,
    val isTestingConnection: Boolean = false,
    val currentLocation: LocationData? = null,
    val connectionStatus: String = "未连接",
    val serverUri: String = "tcp://broker.hivemq.com:1883",
    val clientId: String = "android_client_${System.currentTimeMillis()}",
    val username: String = "",
    val password: String = "",
    val topic: String = "location/tracker",
    val errorMessage: String? = null
)