package com.example.mqttlocationtracker.utils

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * 权限请求管理器
 */
class PermissionManager(private val activity: Activity) {
    
    companion object {
        private const val TAG = "PermissionManager"
    }
    
    // 权限状态LiveData
    private val _locationPermissionStatus = MutableLiveData<PermissionStatus>()
    val locationPermissionStatus = _locationPermissionStatus
    
    /**
     * 权限状态枚举
     */
    enum class PermissionStatus {
        GRANTED,          // 已授予
        DENIED,           // 被拒绝
        DENIED_FOREVER    // 永久拒绝（不再询问）
    }
    
    /**
     * 检查并请求位置权限
     */
    fun checkAndRequestLocationPermissions() {
        Log.d(TAG, "Checking location permissions")
        
        // 首先检查前台定位权限
        if (PermissionHelper.hasFineLocationPermission(activity) || 
            PermissionHelper.hasCoarseLocationPermission(activity)) {
            
            // 检查后台定位权限（Android 10及以上）
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                if (PermissionHelper.hasBackgroundLocationPermission(activity)) {
                    Log.d(TAG, "All location permissions granted")
                    _locationPermissionStatus.value = PermissionStatus.GRANTED
                } else {
                    Log.d(TAG, "Foreground location permission granted, requesting background permission")
                    requestBackgroundLocationPermission()
                }
            } else {
                Log.d(TAG, "Location permissions granted")
                _locationPermissionStatus.value = PermissionStatus.GRANTED
            }
        } else {
            Log.d(TAG, "Requesting foreground location permissions")
            requestForegroundLocationPermissions()
        }
    }
    
    /**
     * 请求前台定位权限
     */
    private fun requestForegroundLocationPermissions() {
        PermissionHelper.requestForegroundLocationPermissions(activity)
    }
    
    /**
     * 请求后台定位权限
     */
    private fun requestBackgroundLocationPermission() {
        PermissionHelper.requestBackgroundLocationPermission(activity)
    }
    
    /**
     * 处理权限请求结果
     */
    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray): Boolean {
        return when (requestCode) {
            PermissionHelper.LOCATION_PERMISSION_REQUEST_CODE -> {
                handleForegroundLocationPermissionsResult(grantResults)
                true
            }
            PermissionHelper.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                handleBackgroundLocationPermissionsResult(grantResults)
                true
            }
            else -> false
        }
    }
    
    /**
     * 处理前台定位权限请求结果
     */
    private fun handleForegroundLocationPermissionsResult(grantResults: IntArray) {
        // 检查是否有任何一个权限被授予
        val isGranted = grantResults.isNotEmpty() && grantResults.any { it == android.content.pm.PackageManager.PERMISSION_GRANTED }
        
        if (isGranted) {
            Log.d(TAG, "Foreground location permission granted")
            
            // 对于Android 10及以上版本，还需要请求后台定位权限
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                requestBackgroundLocationPermission()
            } else {
                _locationPermissionStatus.value = PermissionStatus.GRANTED
            }
        } else {
            Log.d(TAG, "Foreground location permission denied")
            _locationPermissionStatus.value = PermissionStatus.DENIED
        }
    }
    
    /**
     * 处理后台定位权限请求结果
     */
    private fun handleBackgroundLocationPermissionsResult(grantResults: IntArray) {
        val isGranted = grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (isGranted) {
            Log.d(TAG, "Background location permission granted")
            _locationPermissionStatus.value = PermissionStatus.GRANTED
        } else {
            Log.d(TAG, "Background location permission denied")
            _locationPermissionStatus.value = PermissionStatus.DENIED
        }
    }
    
    /**
     * 检查是否需要显示权限解释
     */
    fun shouldShowPermissionRationale(): Boolean {
        return PermissionHelper.shouldShowFineLocationRationale(activity) ||
               PermissionHelper.shouldShowBackgroundLocationRationale(activity)
    }
    
    /**
     * 观察权限状态变化
     */
    fun observePermissionStatus(owner: LifecycleOwner, observer: Observer<PermissionStatus>) {
        _locationPermissionStatus.observe(owner, observer)
    }
}