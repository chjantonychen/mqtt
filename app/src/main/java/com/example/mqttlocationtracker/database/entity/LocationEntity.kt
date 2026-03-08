package com.example.mqttlocationtracker.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mqttlocationtracker.data.LocationData

/**
 * 位置数据实体类，对应数据库中的位置表
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "accuracy")
    val accuracy: Float? = null,
    
    @ColumnInfo(name = "altitude")
    val altitude: Double? = null,
    
    @ColumnInfo(name = "speed")
    val speed: Float? = null,
    
    @ColumnInfo(name = "bearing")
    val bearing: Float? = null,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 将LocationEntity转换为LocationData
     */
    fun toLocationData(): LocationData {
        return LocationData(
            latitude = latitude,
            longitude = longitude,
            timestamp = timestamp,
            accuracy = accuracy,
            altitude = altitude,
            speed = speed,
            bearing = bearing
        )
    }
    
    /**
     * 从LocationData创建LocationEntity
     */
    companion object {
        fun fromLocationData(locationData: LocationData): LocationEntity {
            return LocationEntity(
                latitude = locationData.latitude,
                longitude = locationData.longitude,
                timestamp = locationData.timestamp,
                accuracy = locationData.accuracy,
                altitude = locationData.altitude,
                speed = locationData.speed,
                bearing = locationData.bearing
            )
        }
    }
}