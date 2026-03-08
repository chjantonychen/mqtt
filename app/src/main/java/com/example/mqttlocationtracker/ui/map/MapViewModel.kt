package com.example.mqttlocationtracker.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.example.mqttlocationtracker.database.repository.LocationRepository
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LocationRepository = LocationRepository.getDatabase(application)

    private val _mapLocations = MutableLiveData<List<LocationEntity>>()
    val mapLocations: LiveData<List<LocationEntity>> = _mapLocations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadMapData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val locations = repository.getAllLocations()
                _mapLocations.value = locations
            } catch (e: Exception) {
                _errorMessage.value = "加载地图数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}