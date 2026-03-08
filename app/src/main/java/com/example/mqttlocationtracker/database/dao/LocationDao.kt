package com.example.mqttlocationtracker.database.dao

import androidx.room.*
import com.example.mqttlocationtracker.database.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * 位置数据访问对象接口
 */
@Dao
interface LocationDao {
    
    /**
     * 插入位置数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long
    
    /**
     * 批量插入位置数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)
    
    /**
     * 更新位置数据
     */
    @Update
    suspend fun updateLocation(location: LocationEntity)
    
    /**
     * 删除位置数据
     */
    @Delete
    suspend fun deleteLocation(location: LocationEntity)
    
    /**
     * 根据ID获取位置数据
     */
    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationById(id: Long): LocationEntity?
    
    /**
     * 获取所有位置数据（按时间倒序）
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<LocationEntity>>
    
    /**
     * 获取指定时间范围内的位置数据
     */
    @Query("SELECT * FROM locations WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getLocationsByTimeRange(startTime: Long, endTime: Long): Flow<List<LocationEntity>>
    
    /**
     * 获取最新的N条位置数据
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestLocations(limit: Int): List<LocationEntity>
    
    /**
     * 获取未同步的位置数据
     */
    @Query("SELECT * FROM locations WHERE is_synced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedLocations(): Flow<List<LocationEntity>>
    
    /**
     * 获取未同步的位置数据（限制数量）
     */
    @Query("SELECT * FROM locations WHERE is_synced = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getUnsyncedLocations(limit: Int): List<LocationEntity>
    
    /**
     * 标记位置数据为已同步
     */
    @Query("UPDATE locations SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)
    
    /**
     * 删除指定时间之前的位置数据
     */
    @Query("DELETE FROM locations WHERE timestamp < :beforeTime")
    suspend fun deleteLocationsBefore(beforeTime: Long)
    
    /**
     * 获取位置数据总数
     */
    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getLocationCount(): Int
    
    /**
     * 获取未同步的位置数据数量
     */
    @Query("SELECT COUNT(*) FROM locations WHERE is_synced = 0")
    suspend fun getUnsyncedLocationCount(): Int
    
    /**
     * 获取指定时间范围内的位置数据（挂起函数形式）
     */
    @Query("SELECT * FROM locations WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getLocationsBetweenDates(startTime: Long, endTime: Long): List<LocationEntity>
    
    /**
     * 根据精度过滤位置数据
     */
    @Query("SELECT * FROM locations WHERE accuracy <= :maxAccuracy ORDER BY timestamp ASC")
    suspend fun getLocationsByAccuracy(maxAccuracy: Float): List<LocationEntity>
}