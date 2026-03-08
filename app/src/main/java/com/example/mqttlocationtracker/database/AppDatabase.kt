package com.example.mqttlocationtracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mqttlocationtracker.database.dao.LocationDao
import com.example.mqttlocationtracker.database.entity.LocationEntity

/**
 * 应用数据库类
 */
@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * 获取位置数据访问对象
     */
    abstract fun locationDao(): LocationDao
    
    companion object {
        private const val DATABASE_NAME = "mqtt_location_tracker_db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * 获取数据库实例（单例模式）
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}