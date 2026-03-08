package com.example.mqttlocationtracker.database.repository

import android.content.Context
import com.example.mqttlocationtracker.database.AppDatabase
import com.example.mqttlocationtracker.database.dao.LocationDao
import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.example.mqttlocationtracker.utils.Logger
import kotlinx.coroutines.flow.Flow

/**
 * 位置数据仓库类，提供统一的数据访问接口
 */
class LocationRepository(context: Context) {
    
    private val database: AppDatabase = AppDatabase.getInstance(context)
    private val locationDao: LocationDao = database.locationDao()
    
    companion object {
        private const val TAG = "LocationRepository"
        private const val MAX_UNSYNCED_LOCATIONS = 1000 // 最大未同步位置数据数量
    }
    
    /**
     * 插入位置数据
     */
    suspend fun insertLocation(location: LocationEntity): Long {
        return try {
            val id = locationDao.insertLocation(location)
            Logger.d(TAG, "Location inserted with id: $id")
            id
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to insert location", e)
            throw e
        }
    }
    
    /**
     * 批量插入位置数据
     */
    suspend fun insertLocations(locations: List<LocationEntity>) {
        try {
            locationDao.insertLocations(locations)
            Logger.d(TAG, "${locations.size} locations inserted")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to insert locations", e)
            throw e
        }
    }
    
    /**
     * 获取所有位置数据（Flow形式）
     */
    fun getAllLocations(): Flow<List<LocationEntity>> {
        Logger.d(TAG, "Getting all locations flow")
        return locationDao.getAllLocations()
    }
    
    /**
     * 获取指定时间范围内的位置数据（Flow形式）
     */
    fun getLocationsByTimeRange(startTime: Long, endTime: Long): Flow<List<LocationEntity>> {
        Logger.d(TAG, "Getting locations by time range: $startTime - $endTime")
        return locationDao.getLocationsByTimeRange(startTime, endTime)
    }
    
    /**
     * 获取最新的N条位置数据
     */
    suspend fun getLatestLocations(limit: Int): List<LocationEntity> {
        return try {
            val locations = locationDao.getLatestLocations(limit)
            Logger.d(TAG, "Got ${locations.size} latest locations")
            locations
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to get latest locations", e)
            throw e
        }
    }
    
    /**
     * 获取未同步的位置数据（Flow形式）
     */
    fun getUnsyncedLocations(): Flow<List<LocationEntity>> {
        Logger.d(TAG, "Getting unsynced locations flow")
        return locationDao.getUnsyncedLocations()
    }
    
    /**
     * 获取未同步的位置数据（限制数量）
     */
    suspend fun getUnsyncedLocations(limit: Int = MAX_UNSYNCED_LOCATIONS): List<LocationEntity> {
        return try {
            val locations = locationDao.getUnsyncedLocations(limit)
            Logger.d(TAG, "Got ${locations.size} unsynced locations")
            locations
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to get unsynced locations", e)
            throw e
        }
    }
    
    /**
     * 标记位置数据为已同步
     */
    suspend fun markAsSynced(ids: List<Long>) {
        try {
            locationDao.markAsSynced(ids)
            Logger.d(TAG, "${ids.size} locations marked as synced")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to mark locations as synced", e)
            throw e
        }
    }
    
    /**
     * 删除指定时间之前的位置数据
     */
    suspend fun deleteLocationsBefore(beforeTime: Long) {
        try {
            locationDao.deleteLocationsBefore(beforeTime)
            Logger.d(TAG, "Deleted locations before $beforeTime")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to delete old locations", e)
            throw e
        }
    }
    
    /**
     * 获取位置数据总数
     */
    suspend fun getLocationCount(): Int {
        return try {
            val count = locationDao.getLocationCount()
            Logger.d(TAG, "Total location count: $count")
            count
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to get location count", e)
            throw e
        }
    }
    
    /**
     * 获取未同步的位置数据数量
     */
    suspend fun getUnsyncedLocationCount(): Int {
        return try {
            val count = locationDao.getUnsyncedLocationCount()
            Logger.d(TAG, "Unsynced location count: $count")
            count
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to get unsynced location count", e)
            throw e
        }
    }
}