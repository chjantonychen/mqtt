package com.example.mqttlocationtracker.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 权限管理工具类
 */
object PermissionHelper {
    
    // 权限请求码
    const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002
    
    /**
     * 检查是否已获得精确定位权限
     */
    fun hasFineLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查是否已获得粗略定位权限
     */
    fun hasCoarseLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查是否已获得后台定位权限（Android 10及以上版本需要）
     */
    fun hasBackgroundLocationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 10以下版本不需要特殊后台权限
            hasFineLocationPermission(context) || hasCoarseLocationPermission(context)
        }
    }
    
    /**
     * 请求前台定位权限（精确定位和粗略定位）
     */
    fun requestForegroundLocationPermissions(activity: Activity) {
        val permissions = mutableListOf<String>()
        
        if (!hasFineLocationPermission(activity)) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        if (!hasCoarseLocationPermission(activity)) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissions.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * 请求后台定位权限（仅Android 10及以上版本需要）
     */
    fun requestBackgroundLocationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasBackgroundLocationPermission(activity)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    
    /**
     * 打开应用设置页面，让用户手动授予权限
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
    
    /**
     * 是否需要显示权限解释（仅在用户之前拒绝过权限时返回true）
     */
    fun shouldShowFineLocationRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    
    /**
     * 是否需要显示后台定位权限解释
     */
    fun shouldShowBackgroundLocationRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            false
        }
    }
}