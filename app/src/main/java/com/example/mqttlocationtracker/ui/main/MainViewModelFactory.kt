package com.example.mqttlocationtracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mqttlocationtracker.mqtt.MqttManager
import com.example.mqttlocationtracker.ui.settings.SettingsManager

/**
 * MainViewModel工厂类
 */
class MainViewModelFactory(
    private val mqttManager: MqttManager,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(mqttManager, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}