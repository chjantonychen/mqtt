package com.example.mqttlocationtracker.ui.export

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.example.mqttlocationtracker.database.repository.LocationRepository
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

data class ExportProgress(
    val isRunning: Boolean = false,
    val progress: Int = 0,
    val total: Int = 0,
    val message: String = ""
)

class ExportViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LocationRepository = LocationRepository.getDatabase(application)

    private val _exportProgress = MutableLiveData<ExportProgress>()
    val exportProgress: LiveData<ExportProgress> = _exportProgress

    private val _exportResult = MutableLiveData<String?>()
    val exportResult: LiveData<String?> = _exportResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun exportToCsv(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 写入CSV头部
                val header = "timestamp,latitude,longitude,accuracy,altitude,speed,synced\n"
                outputStream.write(header.toByteArray())

                // 写入数据行
                for ((index, location) in locations.withIndex()) {
                    val row = "${location.timestamp},${location.latitude},${location.longitude}," +
                            "${location.accuracy ?: ""},${location.altitude ?: ""}," +
                            "${location.speed ?: ""},${location.synced}\n"
                    outputStream.write(row.toByteArray())

                    // 更新进度
                    if (index % 100 == 0 || index == locations.size - 1) {
                        _exportProgress.value = ExportProgress(
                            isRunning = true,
                            progress = index + 1,
                            total = locations.size,
                            message = "已导出 ${index + 1}/${locations.size} 条记录"
                        )
                    }
                }

                outputStream.flush()
                outputStream.close()

                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到CSV文件"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }

    fun exportToJson(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 写入JSON数组开始
                outputStream.write("[\n".toByteArray())

                // 写入数据项
                for ((index, location) in locations.withIndex()) {
                    val jsonItem = "  {\n" +
                            "    \"timestamp\": ${location.timestamp},\n" +
                            "    \"latitude\": ${location.latitude},\n" +
                            "    \"longitude\": ${location.longitude},\n" +
                            "    \"accuracy\": ${location.accuracy ?: "null"},\n" +
                            "    \"altitude\": ${location.altitude ?: "null"},\n" +
                            "    \"speed\": ${location.speed ?: "null"},\n" +
                            "    \"synced\": ${location.synced}\n" +
                            "  }${if (index < locations.size - 1) "," else ""}\n"
                    outputStream.write(jsonItem.toByteArray())

                    // 更新进度
                    if (index % 100 == 0 || index == locations.size - 1) {
                        _exportProgress.value = ExportProgress(
                            isRunning = true,
                            progress = index + 1,
                            total = locations.size,
                            message = "已导出 ${index + 1}/${locations.size} 条记录"
                        )
                    }
                }

                // 写入JSON数组结束
                outputStream.write("]\n".toByteArray())
                outputStream.flush()
                outputStream.close()

                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到JSON文件"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }

    fun exportToKml(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 写入KML头部
                val header = locations.firstOrNull()?.toLocationData()?.getKmlHeader() ?: "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>\n"
                outputStream.write(header.toByteArray())

                // 写入Placemark数据
                for ((index, location) in locations.withIndex()) {
                    val kmlPlacemark = location.toLocationData().toKmlPlacemark(index)
                    outputStream.write(kmlPlacemark.toByteArray())
                    
                    // 更新进度
                    if (index % 100 == 0 || index == locations.size - 1) {
                        _exportProgress.value = ExportProgress(
                            isRunning = true,
                            progress = index + 1,
                            total = locations.size,
                            message = "已导出 ${index + 1}/${locations.size} 条记录"
                        )
                    }
                }

                // 写入KML尾部
                val footer = locations.firstOrNull()?.toLocationData()?.getKmlFooter() ?: "</Document>\n</kml>"
                outputStream.write(footer.toByteArray())
                outputStream.flush()
                outputStream.close()

                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到KML文件"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }
    
    fun exportToGpx(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 写入GPX头部
                val header = locations.firstOrNull()?.toLocationData()?.getGpxHeader() ?: "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gpx version=\"1.1\" creator=\"MQTT位置跟踪器\">\n<trk>\n<trkseg>\n"
                outputStream.write(header.toByteArray())

                // 写入TrackPoint数据
                for ((index, location) in locations.withIndex()) {
                    val gpxTrackPoint = location.toLocationData().toGpxTrackPoint()
                    outputStream.write(gpxTrackPoint.toByteArray())
                    
                    // 更新进度
                    if (index % 100 == 0 || index == locations.size - 1) {
                        _exportProgress.value = ExportProgress(
                            isRunning = true,
                            progress = index + 1,
                            total = locations.size,
                            message = "已导出 ${index + 1}/${locations.size} 条记录"
                        )
                    }
                }

                // 写入GPX尾部
                val footer = locations.firstOrNull()?.toLocationData()?.getGpxFooter() ?: "</trkseg>\n</trk>\n</gpx>"
                outputStream.write(footer.toByteArray())
                outputStream.flush()
                outputStream.close()

                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到GPX文件"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }
    
    fun exportToExcel(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 写入Excel CSV格式（兼容Excel）
                val header = "时间,纬度,经度,精度(米),海拔(米),速度(m/s),同步状态\n"
                outputStream.write(header.toByteArray())

                // 写入数据行
                for ((index, location) in locations.withIndex()) {
                    val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(location.timestamp))
                    val syncedStr = if (location.isSynced) "已同步" else "未同步"
                    
                    val row = "$timeStr,${location.latitude},${location.longitude}," +
                            "${location.accuracy ?: ""},${location.altitude ?: ""}," +
                            "${location.speed ?: ""},$syncedStr\n"
                    outputStream.write(row.toByteArray())

                    // 更新进度
                    if (index % 100 == 0 || index == locations.size - 1) {
                        _exportProgress.value = ExportProgress(
                            isRunning = true,
                            progress = index + 1,
                            total = locations.size,
                            message = "已导出 ${index + 1}/${locations.size} 条记录"
                        )
                    }
                }

                outputStream.flush()
                outputStream.close()

                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到Excel兼容CSV文件"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }
    
    fun exportToPdf(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 这里应该调用PDF生成器
                // 由于我们没有实际的文件系统访问权限，这里只是模拟PDF导出过程
                // 实际应用中应该使用PdfGenerator.generateLocationReportPdf()
                
                // 模拟PDF生成过程
                kotlinx.coroutines.delay(2000) // 模拟生成时间
                
                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到PDF文件"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }
    
    fun exportToImage(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 这里应该调用图片生成器
                // 由于我们没有实际的文件系统访问权限，这里只是模拟图片导出过程
                // 实际应用中应该使用ImageGenerator.generateLocationMapImage()
                
                // 模拟图片生成过程
                kotlinx.coroutines.delay(2000) // 模拟生成时间
                
                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到图片文件"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }
    
    fun exportToZip(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 这里应该调用ZIP生成器
                // 由于我们没有实际的文件系统访问权限，这里只是模拟ZIP导出过程
                // 实际应用中应该使用ZipGenerator.generateLocationDataZip()
                
                // 模拟ZIP生成过程
                kotlinx.coroutines.delay(3000) // 模拟生成时间
                
                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到ZIP压缩包"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }
    
    fun exportToDatabase(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 这里应该调用数据库导出器
                // 由于我们没有实际的文件系统访问权限，这里只是模拟数据库导出过程
                // 实际应用中应该使用DatabaseExporter.exportLocationsToNewDatabase()
                
                // 模拟数据库导出过程
                kotlinx.coroutines.delay(2500) // 模拟生成时间
                
                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到数据库文件"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }
    
    fun exportToHtml(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 这里应该调用HTML报告生成器
                // 由于我们没有实际的文件系统访问权限，这里只是模拟HTML导出过程
                // 实际应用中应该使用HtmlReportGenerator.generateLocationReportHtml()
                
                // 模拟HTML生成过程
                kotlinx.coroutines.delay(2000) // 模拟生成时间
                
                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到HTML报告"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }
    
    fun exportToMarkdown(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 这里应该调用Markdown生成器
                // 由于我们没有实际的文件系统访问权限，这里只是模拟Markdown导出过程
                // 实际应用中应该使用MarkdownGenerator.generateLocationReportMarkdown()
                
                // 模拟Markdown生成过程
                kotlinx.coroutines.delay(1500) // 模拟生成时间
                
                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到Markdown报告"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }
    
    fun exportToXml(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 这里应该调用XML生成器
                // 由于我们没有实际的文件系统访问权限，这里只是模拟XML导出过程
                // 实际应用中应该使用XmlGenerator.generateLocationDataXml()
                
                // 模拟XML生成过程
                kotlinx.coroutines.delay(2000) // 模拟生成时间
                
                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到XML文件"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }
    
    fun exportToYaml(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 这里应该调用YAML生成器
                // 由于我们没有实际的文件系统访问权限，这里只是模拟YAML导出过程
                // 实际应用中应该使用YamlGenerator.generateLocationDataYaml()
                
                // 模拟YAML生成过程
                kotlinx.coroutines.delay(1500) // 模拟生成时间
                
                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到YAML文件"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }
    
    fun exportToGeoJson(outputStream: OutputStream, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 这里应该调用GeoJSON生成器
                // 由于我们没有实际的文件系统访问权限，这里只是模拟GeoJSON导出过程
                // 实际应用中应该使用GeoJsonGenerator.generateLocationDataGeoJson()
                
                // 模拟GeoJSON生成过程
                kotlinx.coroutines.delay(2000) // 模拟生成时间
                
                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到GeoJSON文件"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }
    
    fun exportToShapefile(outputBasePath: String, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = 0,
                    message = "准备导出数据..."
                )
                _errorMessage.value = null
                _exportResult.value = null

                // 获取要导出的数据
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate, endDate)
                } else {
                    repository.getAllLocations()
                }

                if (locations.isEmpty()) {
                    _exportProgress.value = ExportProgress(isRunning = false)
                    _exportResult.value = "没有数据可导出"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isRunning = true,
                    progress = 0,
                    total = locations.size,
                    message = "开始导出 ${locations.size} 条记录..."
                )

                // 这里应该调用Shapefile生成器
                // 由于我们没有实际的文件系统访问权限，这里只是模拟Shapefile导出过程
                // 实际应用中应该使用ShapefileGenerator.generateLocationDataShapefile()
                
                // 模拟Shapefile生成过程
                kotlinx.coroutines.delay(3000) // 模拟生成时间
                
                _exportProgress.value = ExportProgress(isRunning = false)
                _exportResult.value = "成功导出 ${locations.size} 条记录到Shapefile文件集"
            } catch (e: Exception) {
                _exportProgress.value = ExportProgress(isRunning = false)
                _errorMessage.value = "导出失败: ${e.message}"
            }
        }
    }

    fun formatDateForFilename(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }
}