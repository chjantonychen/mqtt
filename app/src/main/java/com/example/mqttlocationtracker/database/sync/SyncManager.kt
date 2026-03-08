package com.example.mqttlocationtracker.database.sync

import com.example.mqttlocationtracker.database.repository.LocationRepository
import com.example.mqttlocationtracker.mqtt.MqttManager
import com.example.mqttlocationtracker.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 数据同步管理器，负责处理离线数据的同步
 */
class SyncManager(
    private val locationRepository: LocationRepository,
    private val mqttManager: MqttManager,
    private val coroutineScope: CoroutineScope
) {
    
    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_BATCH_SIZE = 50 // 每次同步的最大数据量
    }
    
    /**
     * 同步未发送的位置数据
     */
    fun syncUnsentLocations(mqttTopic: String) {
        if (!mqttManager.isConnected()) {
            Logger.d(TAG, "MQTT not connected, skipping sync")
            return
        }
        
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // 获取未同步的位置数据
                val unsyncedLocations = locationRepository.getUnsyncedLocations(SYNC_BATCH_SIZE)
                
                if (unsyncedLocations.isEmpty()) {
                    Logger.d(TAG, "No unsynced locations to send")
                    return@launch
                }
                
                Logger.d(TAG, "Syncing ${unsyncedLocations.size} unsynced locations")
                
                // 发送未同步的数据
                val sentIds = mutableListOf<Long>()
                
                for (locationEntity in unsyncedLocations) {
                    try {
                        val locationData = locationEntity.toLocationData()
                        mqttManager.publish(mqttTopic, locationData.toJson())
                        sentIds.add(locationEntity.id)
                        Logger.d(TAG, "Sent location ${locationEntity.id} to MQTT")
                    } catch (e: Exception) {
                        Logger.e(TAG, "Failed to send location ${locationEntity.id} to MQTT", e)
                        // 继续处理下一个位置数据
                    }
                }
                
                // 标记已发送的数据为已同步
                if (sentIds.isNotEmpty()) {
                    locationRepository.markAsSynced(sentIds)
                    Logger.d(TAG, "Marked ${sentIds.size} locations as synced")
                }
                
            } catch (e: Exception) {
                Logger.e(TAG, "Error during location sync", e)
            }
        }
    }
    
    /**
     * 检查是否有未同步的数据
     */
    suspend fun hasUnsyncedData(): Boolean {
        return try {
            val count = locationRepository.getUnsyncedLocationCount()
            Logger.d(TAG, "Unsynced location count: $count")
            count > 0
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking for unsynced data", e)
            false
        }
    }
}