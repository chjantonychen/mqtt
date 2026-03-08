package com.example.mqttlocationtracker.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.example.mqttlocationtracker.database.repository.LocationRepository
import kotlinx.coroutines.launch
import java.util.Date

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LocationRepository = LocationRepository.getDatabase(application)

    private val _historyLocations = MutableLiveData<List<LocationEntity>>()
    val historyLocations: LiveData<List<LocationEntity>> = _historyLocations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var startDate: Date? = null
    private var endDate: Date? = null

    fun loadHistoryData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val locations = if (startDate != null && endDate != null) {
                    repository.getLocationsBetweenDates(startDate!!, endDate!!)
                } else {
                    repository.getAllLocations()
                }
                
                _historyLocations.value = locations
            } catch (e: Exception) {
                _errorMessage.value = "加载历史数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setDateRange(start: Date, end: Date) {
        startDate = start
        endDate = end
        loadHistoryData()
    }

    fun clearDateRange() {
        startDate = null
        endDate = null
        loadHistoryData()
    }
}