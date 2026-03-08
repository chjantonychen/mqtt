package com.example.mqttlocationtracker.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.example.mqttlocationtracker.database.repository.LocationRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class StatsData(
    val totalLocations: Int = 0,
    val todayLocations: Int = 0,
    val unsyncedLocations: Int = 0,
    val firstLocationTime: Long? = null,
    val lastLocationTime: Long? = null,
    val averageAccuracy: Float? = null,
    val totalDistance: Float = 0f
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LocationRepository = LocationRepository.getDatabase(application)

    private val _statsData = MutableLiveData<StatsData>()
    val statsData: LiveData<StatsData> = _statsData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadStatsData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // 获取所有位置数据
                val allLocations = repository.getAllLocations()

                // 计算统计数据
                val totalLocations = allLocations.size
                val todayStart = getTodayStartTime()
                val todayLocations = allLocations.count { it.timestamp >= todayStart }
                val unsyncedLocations = allLocations.count { !it.synced }

                val firstLocationTime = allLocations.minByOrNull { it.timestamp }?.timestamp
                val lastLocationTime = allLocations.maxByOrNull { it.timestamp }?.timestamp

                val averageAccuracy = if (allLocations.isNotEmpty()) {
                    val validAccuracies = allLocations.mapNotNull { it.accuracy }
                    if (validAccuracies.isNotEmpty()) {
                        (validAccuracies.average().toFloat())
                    } else null
                } else null

                val totalDistance = calculateTotalDistance(allLocations)

                val statsData = StatsData(
                    totalLocations = totalLocations,
                    todayLocations = todayLocations,
                    unsyncedLocations = unsyncedLocations,
                    firstLocationTime = firstLocationTime,
                    lastLocationTime = lastLocationTime,
                    averageAccuracy = averageAccuracy,
                    totalDistance = totalDistance
                )

                _statsData.value = statsData
            } catch (e: Exception) {
                _errorMessage.value = "加载统计数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getTodayStartTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun calculateTotalDistance(locations: List<LocationEntity>): Float {
        if (locations.size < 2) return 0f

        var totalDistance = 0f
        for (i in 1 until locations.size) {
            val prevLocation = locations[i - 1]
            val currLocation = locations[i]
            totalDistance += calculateDistance(
                prevLocation.latitude, prevLocation.longitude,
                currLocation.latitude, currLocation.longitude
            )
        }
        return totalDistance
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val earthRadius = 6371000 // 地球半径（米）
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }

    fun formatDate(timestamp: Long?): String {
        return if (timestamp != null) {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
        } else {
            "无数据"
        }
    }

    fun formatDistance(distanceMeters: Float): String {
        return when {
            distanceMeters < 1000 -> "${distanceMeters.roundToInt()} 米"
            else -> "${String.format("%.2f", distanceMeters / 1000)} 公里"
        }
    }
}