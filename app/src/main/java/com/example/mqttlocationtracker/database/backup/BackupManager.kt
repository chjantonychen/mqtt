package com.example.mqttlocationtracker.database.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.example.mqttlocationtracker.database.repository.LocationRepository
import com.example.mqttlocationtracker.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 数据备份管理器，负责备份和恢复位置数据
 */
class BackupManager(
    private val context: Context,
    private val locationRepository: LocationRepository,
    private val coroutineScope: CoroutineScope
) {
    
    companion object {
        private const val TAG = "BackupManager"
        private const val BACKUP_FILE_PREFIX = "mqtt_location_backup_"
        private const val BACKUP_FILE_EXTENSION_JSON = ".json"
        private const val BACKUP_FILE_EXTENSION_CSV = ".csv"
        private const val BACKUP_FILE_EXTENSION_SQL = ".sql"
    }
    
    /**
     * 备份格式枚举
     */
    enum class BackupFormat {
        JSON,
        CSV,
        SQL
    }
    
    /**
     * 备份所有位置数据到指定输出流
     */
    fun backupToStream(
        outputStream: OutputStream, 
        format: BackupFormat = BackupFormat.JSON,
        onComplete: (Boolean, String) -> Unit
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                Logger.d(TAG, "开始备份位置数据，格式: $format")
                
                // 获取所有位置数据
                val locations = locationRepository.getAllLocations()
                Logger.d(TAG, "获取到 ${locations.size} 条位置数据")
                
                // 根据格式序列化数据
                val serializedData = when (format) {
                    BackupFormat.JSON -> serializeToJson(locations)
                    BackupFormat.CSV -> serializeToCsv(locations)
                    BackupFormat.SQL -> serializeToSql(locations)
                }
                
                outputStream.write(serializedData.toByteArray())
                outputStream.flush()
                outputStream.close()
                
                Logger.i(TAG, "位置数据备份完成，共 ${locations.size} 条记录，格式: $format")
                withContext(Dispatchers.Main) {
                    onComplete(true, "成功备份 ${locations.size} 条位置数据 (${format})")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "备份位置数据失败", e)
                withContext(Dispatchers.Main) {
                    onComplete(false, "备份失败: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 从指定输入流恢复位置数据
     */
    fun restoreFromStream(inputStream: InputStream, onComplete: (Boolean, String) -> Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                Logger.d(TAG, "开始恢复位置数据")
                
                // 读取输入流内容
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                inputStream.close()
                
                // 反序列化备份数据
                val backupData = deserializeBackupData(jsonString)
                
                // 验证备份数据
                if (backupData.version != 1) {
                    Logger.w(TAG, "不支持的备份文件版本: ${backupData.version}")
                    withContext(Dispatchers.Main) {
                        onComplete(false, "不支持的备份文件版本")
                    }
                    return@launch
                }
                
                // 转换为位置实体
                val locationEntities = backupData.locations.map { it.toLocationEntity() }
                Logger.d(TAG, "解析到 ${locationEntities.size} 条位置数据")
                
                // 插入到数据库
                locationRepository.insertLocations(locationEntities)
                
                Logger.i(TAG, "位置数据恢复完成，共 ${locationEntities.size} 条记录")
                withContext(Dispatchers.Main) {
                    onComplete(true, "成功恢复 ${locationEntities.size} 条位置数据")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "恢复位置数据失败", e)
                withContext(Dispatchers.Main) {
                    onComplete(false, "恢复失败: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 生成默认备份文件名
     */
    fun generateBackupFileName(format: BackupFormat = BackupFormat.JSON): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val extension = when (format) {
            BackupFormat.JSON -> BACKUP_FILE_EXTENSION_JSON
            BackupFormat.CSV -> BACKUP_FILE_EXTENSION_CSV
            BackupFormat.SQL -> BACKUP_FILE_EXTENSION_SQL
        }
        return "$BACKUP_FILE_PREFIX$timestamp$extension"
    }
    
    /**
     * 获取设备信息
     */
    private fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = android.os.Build.MANUFACTURER,
            model = android.os.Build.MODEL,
            androidVersion = android.os.Build.VERSION.RELEASE,
            appId = context.packageName,
            appName = context.applicationInfo.loadLabel(context.packageManager).toString()
        )
    }
    
    /**
     * 序列化为JSON格式
     */
    private fun serializeToJson(locations: List<LocationEntity>): String {
        return try {
            val locationsJson = locations.joinToString(",") { location ->
                """
                {
                    "id": ${location.id},
                    "latitude": ${location.latitude},
                    "longitude": ${location.longitude},
                    "accuracy": ${location.accuracy ?: "null"},
                    "altitude": ${location.altitude ?: "null"},
                    "speed": ${location.speed ?: "null"},
                    "timestamp": ${location.timestamp},
                    "synced": ${location.synced}
                }
                """.trimIndent()
            }
            
            """
            {
                "version": 1,
                "timestamp": ${System.currentTimeMillis()},
                "deviceInfo": {
                    "manufacturer": "${getDeviceInfo().manufacturer}",
                    "model": "${getDeviceInfo().model}",
                    "androidVersion": "${getDeviceInfo().androidVersion}",
                    "appId": "${getDeviceInfo().appId}",
                    "appName": "${getDeviceInfo().appName}"
                },
                "locations": [$locationsJson]
            }
            """.trimIndent()
        } catch (e: Exception) {
            Logger.e(TAG, "序列化JSON数据失败", e)
            "{}"
        }
    }
    
    /**
     * 序列化为CSV格式
     */
    private fun serializeToCsv(locations: List<LocationEntity>): String {
        return try {
            val header = "id,latitude,longitude,accuracy,altitude,speed,timestamp,synced\n"
            val rows = locations.joinToString("\n") { location ->
                "${location.id},${location.latitude},${location.longitude}," +
                "${location.accuracy ?: ""},${location.altitude ?: ""},${location.speed ?: ""}," +
                "${location.timestamp},${location.synced}"
            }
            
            header + rows
        } catch (e: Exception) {
            Logger.e(TAG, "序列化CSV数据失败", e)
            ""
        }
    }
    
    /**
     * 序列化为SQL格式
     */
    private fun serializeToSql(locations: List<LocationEntity>): String {
        return try {
            val sqlBuilder = StringBuilder()
            sqlBuilder.append("-- MQTT位置跟踪器备份文件\n")
            sqlBuilder.append("-- 生成时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")
            
            // 创建表语句
            sqlBuilder.append("""
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
                );
                
            """.trimIndent())
            
            // 插入数据语句
            locations.forEach { location ->
                sqlBuilder.append(
                    "INSERT INTO locations (id, latitude, longitude, timestamp, accuracy, altitude, speed, is_synced) VALUES " +
                    "(${location.id}, ${location.latitude}, ${location.longitude}, ${location.timestamp}, " +
                    "${location.accuracy ?: "NULL"}, ${location.altitude ?: "NULL"}, ${location.speed ?: "NULL"}, " +
                    "${if (location.isSynced) 1 else 0});\n"
                )
            }
            
            sqlBuilder.toString()
        } catch (e: Exception) {
            Logger.e(TAG, "序列化SQL数据失败", e)
            ""
        }
    }
    
    /**
     * 反序列化JSON字符串为备份数据
     */
    private fun deserializeBackupData(jsonString: String): BackupData {
        // 简单的JSON反序列化实现（实际应用中应使用专业的JSON库）
        // 这里只是一个示例实现
        return BackupData(
            version = 1,
            timestamp = System.currentTimeMillis(),
            deviceInfo = DeviceInfo("", "", "", "", ""),
            locations = emptyList()
        )
    }
}

/**
 * 备份数据结构
 */
data class BackupData(
    val version: Int,
    val timestamp: Long,
    val deviceInfo: DeviceInfo,
    val locations: List<BackupLocation>
)

/**
 * 设备信息
 */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val appId: String,
    val appName: String
)

/**
 * 备份位置数据
 */
data class BackupLocation(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val altitude: Double?,
    val speed: Float?,
    val timestamp: Long,
    val synced: Boolean
)

/**
 * 扩展函数：将LocationEntity转换为BackupLocation
 */
fun LocationEntity.toBackupLocation(): BackupLocation {
    return BackupLocation(
        id = this.id,
        latitude = this.latitude,
        longitude = this.longitude,
        accuracy = this.accuracy,
        altitude = this.altitude,
        speed = this.speed,
        timestamp = this.timestamp,
        synced = this.synced
    )
}

/**
 * 扩展函数：将BackupLocation转换为LocationEntity
 */
fun BackupLocation.toLocationEntity(): LocationEntity {
    return LocationEntity(
        id = this.id,
        latitude = this.latitude,
        longitude = this.longitude,
        accuracy = this.accuracy,
        altitude = this.altitude,
        speed = this.speed,
        timestamp = this.timestamp,
        synced = this.synced
    )
}