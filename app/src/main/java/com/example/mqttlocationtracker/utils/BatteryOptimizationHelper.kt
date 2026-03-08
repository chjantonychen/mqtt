package com.example.mqttlocationtracker.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat.getSystemService

/**
 * 电池优化助手类，用于检查和处理电池优化设置
 */
object BatteryOptimizationHelper {
    
    private const val TAG = "BatteryOptimizationHelper"
    
    /**
     * 检查应用是否被电池优化忽略
     */
    @SuppressLint("BatteryLife")
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true // 低版本Android默认不受电池优化影响
            }
        } catch (e: Exception) {
            Logger.e(TAG, "检查电池优化设置失败", e)
            false
        }
    }
    
    /**
     * 请求忽略电池优化
     * 注意：这需要用户手动确认
     */
    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations(context: Context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "请求忽略电池优化失败", e)
        }
    }
    
    /**
     * 打开电池优化设置页面
     */
    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            Logger.e(TAG, "打开电池优化设置失败", e)
        }
    }
    
    /**
     * 获取电池优化建议文本
     */
    fun getBatteryOptimizationAdvice(context: Context): String {
        return if (isIgnoringBatteryOptimizations(context)) {
            "应用已被允许在后台运行，电池优化设置正常。"
        } else {
            "为了确保后台位置跟踪正常工作，建议将此应用添加到电池优化白名单。"
        }
    }
    
    /**
     * 检查是否需要显示电池优化提醒
     */
    fun shouldShowBatteryOptimizationReminder(context: Context): Boolean {
        // 只有在应用未被电池优化忽略且跟踪服务正在运行时才显示提醒
        return !isIgnoringBatteryOptimizations(context)
    }
}