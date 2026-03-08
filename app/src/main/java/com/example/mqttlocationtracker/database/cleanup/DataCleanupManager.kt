package com.example.mqttlocationtracker.database.cleanup

import com.example.mqttlocationtracker.database.repository.LocationRepository
import com.example.mqttlocationtracker.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * 数据清理管理器，负责定期清理旧的历史数据
 */
class DataCleanupManager(
    private val locationRepository: LocationRepository,
    private val coroutineScope: CoroutineScope
) {
    
    companion object {
        private const val TAG = "DataCleanupManager"
        
        // 默认保留30天的数据
        private const val DEFAULT_RETENTION_DAYS = 30L
        
        // 清理检查间隔（24小时）
        private const val CLEANUP_INTERVAL_HOURS = 24L
    }
    
    /**
     * 清理过期的位置数据
     */
    fun cleanupExpiredData(retentionDays: Long = DEFAULT_RETENTION_DAYS) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                val retentionMillis = TimeUnit.DAYS.toMillis(retentionDays)
                val cutoffTime = currentTime - retentionMillis
                
                Logger.d(TAG, "Cleaning up locations older than $retentionDays days (before $cutoffTime)")
                
                // 删除过期的数据
                locationRepository.deleteLocationsBefore(cutoffTime)
                
                Logger.d(TAG, "Expired location data cleanup completed")
            } catch (e: Exception) {
                Logger.e(TAG, "Error during data cleanup", e)
            }
        }
    }
    
    /**
     * 获取建议的清理时间（下次清理时间）
     */
    fun getNextCleanupTime(): Long {
        val currentTime = System.currentTimeMillis()
        return currentTime + TimeUnit.HOURS.toMillis(CLEANUP_INTERVAL_HOURS)
    }
    
    /**
     * 检查是否需要进行清理
     */
    fun shouldCleanup(): Boolean {
        // 简单实现：基于固定时间间隔
        // 在实际应用中，可能需要检查上次清理时间
        return true
    }
}