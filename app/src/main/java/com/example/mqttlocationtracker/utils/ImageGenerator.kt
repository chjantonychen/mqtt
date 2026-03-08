package com.example.mqttlocationtracker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 图片生成器，用于将位置数据导出为图片格式
 */
object ImageGenerator {
    
    private const val TAG = "ImageGenerator"
    private const val IMAGE_WIDTH = 1080 // 图片宽度 (pixels)
    private const val IMAGE_HEIGHT = 1920 // 图片高度 (pixels)
    private const val MARGIN = 50 // 边距 (pixels)
    
    /**
     * 生成位置轨迹图Bitmap
     */
    fun generateLocationMapImage(
        context: Context,
        locations: List<LocationEntity>,
        width: Int = IMAGE_WIDTH,
        height: Int = IMAGE_HEIGHT
    ): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // 绘制背景
            canvas.drawColor(Color.WHITE)
            
            // 绘制标题
            val paint = Paint().apply {
                isAntiAlias = true
                textSize = 48f
                color = Color.BLACK
            }
            
            val title = "位置轨迹图"
            val titleWidth = paint.measureText(title)
            canvas.drawText(title, (width - titleWidth) / 2, MARGIN + 40f, paint)
            
            // 绘制生成时间
            paint.textSize = 32f
            paint.color = Color.GRAY
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val timeText = "生成时间: $timestamp"
            val timeWidth = paint.measureText(timeText)
            canvas.drawText(timeText, (width - timeWidth) / 2, MARGIN + 90f, paint)
            
            // 绘制统计信息
            paint.color = Color.BLACK
            val statsText = "总计: ${locations.size} 个位置点"
            val statsWidth = paint.measureText(statsText)
            canvas.drawText(statsText, (width - statsWidth) / 2, MARGIN + 140f, paint)
            
            // 绘制轨迹图
            if (locations.isNotEmpty()) {
                drawLocationPath(canvas, locations, width, height)
            }
            
            Logger.i(TAG, "Location map image generated successfully")
            bitmap
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate location map image", e)
            null
        }
    }
    
    /**
     * 绘制位置轨迹路径
     */
    private fun drawLocationPath(
        canvas: Canvas,
        locations: List<LocationEntity>,
        width: Int,
        height: Int
    ) {
        if (locations.size < 2) return
        
        val paint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 8f
            color = Color.BLUE
            style = android.graphics.Paint.Style.STROKE
        }
        
        // 计算轨迹的边界
        val minLat = locations.minOf { it.latitude }
        val maxLat = locations.maxOf { it.latitude }
        val minLng = locations.minOf { it.longitude }
        val maxLng = locations.maxOf { it.longitude }
        
        // 计算绘图区域
        val mapLeft = MARGIN.toFloat()
        val mapTop = 200f
        val mapWidth = (width - 2 * MARGIN).toFloat()
        val mapHeight = (height - 300).toFloat()
        
        // 绘制轨迹线
        val path = Path()
        var isFirst = true
        
        for (location in locations) {
            // 将经纬度转换为图片坐标
            val x = mapLeft + ((location.longitude - minLng) / (maxLng - minLng)) * mapWidth
            val y = mapTop + ((maxLat - location.latitude) / (maxLat - minLat)) * mapHeight
            
            if (isFirst) {
                path.moveTo(x, y)
                isFirst = false
            } else {
                path.lineTo(x, y)
            }
        }
        
        canvas.drawPath(path, paint)
        
        // 绘制起点和终点标记
        val startPaint = Paint().apply {
            isAntiAlias = true
            color = Color.GREEN
            style = android.graphics.Paint.Style.FILL
        }
        
        val endPaint = Paint().apply {
            isAntiAlias = true
            color = Color.RED
            style = android.graphics.Paint.Style.FILL
        }
        
        // 起点
        if (locations.isNotEmpty()) {
            val firstLoc = locations.first()
            val startX = mapLeft + ((firstLoc.longitude - minLng) / (maxLng - minLng)) * mapWidth
            val startY = mapTop + ((maxLat - firstLoc.latitude) / (maxLat - minLat)) * mapHeight
            canvas.drawCircle(startX, startY, 15f, startPaint)
        }
        
        // 终点
        if (locations.isNotEmpty()) {
            val lastLoc = locations.last()
            val endX = mapLeft + ((lastLoc.longitude - minLng) / (maxLng - minLng)) * mapWidth
            val endY = mapTop + ((maxLat - lastLoc.latitude) / (maxLat - minLat)) * mapHeight
            canvas.drawCircle(endX, endY, 15f, endPaint)
        }
        
        // 添加图例
        val legendTop = mapTop + mapHeight + 30f
        canvas.drawCircle(MARGIN + 20f, legendTop, 15f, startPaint)
        
        val legendPaint = Paint().apply {
            isAntiAlias = true
            textSize = 36f
            color = Color.BLACK
        }
        
        canvas.drawText("起点", MARGIN + 50f, legendTop + 15f, legendPaint)
        
        canvas.drawCircle(MARGIN + 180f, legendTop, 15f, endPaint)
        canvas.drawText("终点", MARGIN + 210f, legendTop + 15f, legendPaint)
        
        canvas.drawCircle(MARGIN + 340f, legendTop, 10f, paint)
        canvas.drawText("轨迹", MARGIN + 370f, legendTop + 15f, legendPaint)
    }
    
    /**
     * 生成位置数据统计图表Bitmap
     */
    fun generateStatisticsChartImage(
        context: Context,
        locations: List<LocationEntity>,
        width: Int = IMAGE_WIDTH,
        height: Int = IMAGE_HEIGHT
    ): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // 绘制背景
            canvas.drawColor(Color.WHITE)
            
            // 绘制标题
            val paint = Paint().apply {
                isAntiAlias = true
                textSize = 48f
                color = Color.BLACK
            }
            
            val title = "位置数据统计"
            val titleWidth = paint.measureText(title)
            canvas.drawText(title, (width - titleWidth) / 2, MARGIN + 40f, paint)
            
            // 绘制生成时间
            paint.textSize = 32f
            paint.color = Color.GRAY
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val timeText = "生成时间: $timestamp"
            val timeWidth = paint.measureText(timeText)
            canvas.drawText(timeText, (width - timeWidth) / 2, MARGIN + 90f, paint)
            
            // 绘制统计数据
            paint.color = Color.BLACK
            paint.textSize = 36f
            
            val totalLocations = locations.size
            val dateRangeStart = locations.minOfOrNull { it.timestamp } ?: 0
            val dateRangeEnd = locations.maxOfOrNull { it.timestamp } ?: 0
            val dateRangeDays = if (dateRangeStart > 0 && dateRangeEnd > 0) {
                ((dateRangeEnd - dateRangeStart) / (1000 * 60 * 60 * 24)).toInt()
            } else {
                0
            }
            
            val avgAccuracy = locations.mapNotNull { it.accuracy }.average().takeIf { it.isFinite() }
            val avgSpeed = locations.mapNotNull { it.speed }.average().takeIf { it.isFinite() }
            
            var yOffset = MARGIN + 160f
            val lineHeight = 60f
            
            canvas.drawText("总位置数: $totalLocations", MARGIN.toFloat(), yOffset, paint)
            yOffset += lineHeight
            
            if (dateRangeDays > 0) {
                canvas.drawText("数据范围: ${dateRangeDays}天", MARGIN.toFloat(), yOffset, paint)
                yOffset += lineHeight
            }
            
            avgAccuracy?.let {
                canvas.drawText("平均精度: ${String.format("%.1f", it)}米", MARGIN.toFloat(), yOffset, paint)
                yOffset += lineHeight
            }
            
            avgSpeed?.let {
                canvas.drawText("平均速度: ${String.format("%.2f", it)} m/s", MARGIN.toFloat(), yOffset, paint)
                yOffset += lineHeight
            }
            
            Logger.i(TAG, "Statistics chart image generated successfully")
            bitmap
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate statistics chart image", e)
            null
        }
    }
    
    /**
     * 将Bitmap保存为PNG文件
     */
    fun saveBitmapAsPng(bitmap: Bitmap, outputStream: FileOutputStream): Boolean {
        return try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Logger.i(TAG, "Bitmap saved as PNG successfully")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to save bitmap as PNG", e)
            false
        }
    }
    
    /**
     * 将Bitmap保存为JPEG文件
     */
    fun saveBitmapAsJpeg(bitmap: Bitmap, outputStream: FileOutputStream, quality: Int = 90): Boolean {
        return try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            Logger.i(TAG, "Bitmap saved as JPEG successfully")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to save bitmap as JPEG", e)
            false
        }
    }
}