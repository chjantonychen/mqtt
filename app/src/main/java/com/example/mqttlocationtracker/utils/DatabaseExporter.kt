package com.example.mqttlocationtracker.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.mqttlocationtracker.database.AppDatabase
import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

/**
 * 数据库导出器，用于将位置数据导出为SQLite数据库文件
 */
object DatabaseExporter {
    
    private const val TAG = "DatabaseExporter"
    
    /**
     * 导出数据库为SQLite文件
     */
    fun exportDatabaseToFile(
        context: Context,
        outputPath: String
    ): Boolean {
        return try {
            // 获取当前数据库路径
            val dbPath = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            
            if (!dbPath.exists()) {
                Logger.e(TAG, "Database file not found: ${dbPath.absolutePath}")
                return false
            }
            
            // 复制数据库文件
            val outputFile = File(outputPath)
            copyFile(dbPath, outputFile)
            
            Logger.i(TAG, "Database exported successfully to: $outputPath")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to export database", e)
            false
        }
    }
    
    /**
     * 复制文件
     */
    private fun copyFile(sourceFile: File, destFile: File) {
        if (!destFile.parentFile.exists()) {
            destFile.parentFile.mkdirs()
        }
        
        var sourceChannel: FileChannel? = null
        var destChannel: FileChannel? = null
        
        try {
            sourceChannel = FileInputStream(sourceFile).channel
            destChannel = FileOutputStream(destFile).channel
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size())
        } finally {
            sourceChannel?.close()
            destChannel?.close()
        }
    }
    
    /**
     * 导出位置数据为新的SQLite数据库
     */
    fun exportLocationsToNewDatabase(
        context: Context,
        locations: List<LocationEntity>,
        outputPath: String
    ): Boolean {
        return try {
            val outputFile = File(outputPath)
            if (!outputFile.parentFile.exists()) {
                outputFile.parentFile.mkdirs()
            }
            
            // 创建新的SQLite数据库
            val db = SQLiteDatabase.openOrCreateDatabase(outputPath, null)
            
            // 创建表结构
            createLocationsTable(db)
            
            // 插入数据
            insertLocations(db, locations)
            
            db.close()
            
            Logger.i(TAG, "Locations exported to new database successfully: $outputPath")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to export locations to new database", e)
            false
        }
    }
    
    /**
     * 创建位置表
     */
    private fun createLocationsTable(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE IF NOT EXISTS locations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                timestamp INTEGER NOT NULL,
                accuracy REAL,
                altitude REAL,
                speed REAL,
                bearing REAL,
                is_synced INTEGER DEFAULT 0,
                created_at INTEGER DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()
        
        db.execSQL(createTableSql)
    }
    
    /**
     * 插入位置数据
     */
    private fun insertLocations(db: SQLiteDatabase, locations: List<LocationEntity>) {
        db.beginTransaction()
        try {
            for (location in locations) {
                val insertSql = """
                    INSERT INTO locations (
                        id, latitude, longitude, timestamp, accuracy, altitude, speed, bearing, is_synced, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
                
                db.execSQL(insertSql, arrayOf(
                    location.id,
                    location.latitude,
                    location.longitude,
                    location.timestamp,
                    location.accuracy,
                    location.altitude,
                    location.speed,
                    location.bearing,
                    if (location.isSynced) 1 else 0,
                    location.createdAt
                ))
            }
            
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    
    /**
     * 生成数据库导出信息
     */
    fun generateDatabaseInfo(context: Context, locations: List<LocationEntity>): String {
        return try {
            val dbPath = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            val dbFile = if (dbPath.exists()) dbPath else null
            val dbSize = dbFile?.length() ?: 0
            
            """
            数据库导出信息
            ==============
            
            原始数据库:
            - 路径: ${dbPath.absolutePath}
            - 大小: ${formatFileSize(dbSize)}
            
            导出数据:
            - 位置点数量: ${locations.size}
            - 数据范围: ${getDateRangeInfo(locations)}
            - 平均精度: ${getAverageAccuracy(locations)}
            
            使用说明:
            1. 导出的SQLite文件可以在支持SQLite的工具中打开
            2. 表结构与应用内部数据库一致
            3. 可用于数据迁移或备份
            
            由MQTT位置跟踪器生成
            """.trimIndent()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate database info", e)
            "数据库信息生成失败"
        }
    }
    
    /**
     * 格式化文件大小
     */
    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${String.format("%.2f", size / 1024.0)} KB"
            size < 1024 * 1024 * 1024 -> "${String.format("%.2f", size / (1024.0 * 1024.0))} MB"
            else -> "${String.format("%.2f", size / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }
    
    /**
     * 获取数据范围信息
     */
    private fun getDateRangeInfo(locations: List<LocationEntity>): String {
        if (locations.isEmpty()) return "无数据"
        
        val timestamps = locations.map { it.timestamp }
        val minTime = timestamps.minOrNull() ?: 0
        val maxTime = timestamps.maxOrNull() ?: 0
        
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val minDate = dateFormat.format(java.util.Date(minTime))
        val maxDate = dateFormat.format(java.util.Date(maxTime))
        
        return "$minDate 至 $maxDate"
    }
    
    /**
     * 获取平均精度
     */
    private fun getAverageAccuracy(locations: List<LocationEntity>): String {
        val accuracies = locations.mapNotNull { it.accuracy }
        if (accuracies.isEmpty()) return "无精度数据"
        
        val avg = accuracies.average()
        return "${String.format("%.1f", avg)} 米"
    }
}