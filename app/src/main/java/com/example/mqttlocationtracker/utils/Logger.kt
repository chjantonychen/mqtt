package com.example.mqttlocationtracker.utils

import android.util.Log

/**
 * 日志工具类，统一管理应用中的日志输出
 */
object Logger {
    
    // 日志标签前缀
    private const val TAG_PREFIX = "MQTTLocationTracker"
    
    // 是否启用日志输出
    private var isLoggingEnabled = true
    
    /**
     * 设置是否启用日志输出
     */
    fun setLoggingEnabled(enabled: Boolean) {
        isLoggingEnabled = enabled
    }
    
    /**
     * 输出VERBOSE级别的日志
     */
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            val fullTag = "$TAG_PREFIX.$tag"
            if (throwable != null) {
                Log.v(fullTag, message, throwable)
            } else {
                Log.v(fullTag, message)
            }
        }
    }
    
    /**
     * 输出DEBUG级别的日志
     */
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            val fullTag = "$TAG_PREFIX.$tag"
            if (throwable != null) {
                Log.d(fullTag, message, throwable)
            } else {
                Log.d(fullTag, message)
            }
        }
    }
    
    /**
     * 输出INFO级别的日志
     */
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            val fullTag = "$TAG_PREFIX.$tag"
            if (throwable != null) {
                Log.i(fullTag, message, throwable)
            } else {
                Log.i(fullTag, message)
            }
        }
    }
    
    /**
     * 输出WARN级别的日志
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            val fullTag = "$TAG_PREFIX.$tag"
            if (throwable != null) {
                Log.w(fullTag, message, throwable)
            } else {
                Log.w(fullTag, message)
            }
        }
    }
    
    /**
     * 输出ERROR级别的日志
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            val fullTag = "$TAG_PREFIX.$tag"
            if (throwable != null) {
                Log.e(fullTag, message, throwable)
            } else {
                Log.e(fullTag, message)
            }
        }
    }
    
    /**
     * 输出ASSERT级别的日志
     */
    fun wtf(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            val fullTag = "$TAG_PREFIX.$tag"
            if (throwable != null) {
                Log.wtf(fullTag, message, throwable)
            } else {
                Log.wtf(fullTag, message)
            }
        }
    }
}