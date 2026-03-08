package com.example.mqttlocationtracker.utils

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * PDF生成器，用于将位置数据导出为PDF格式
 */
object PdfGenerator {
    
    private const val TAG = "PdfGenerator"
    private const val PAGE_WIDTH = 595 // A4宽度 (points)
    private const val PAGE_HEIGHT = 842 // A4高度 (points)
    private const val MARGIN = 50 // 页边距 (points)
    
    /**
     * 生成位置数据PDF报告
     */
    fun generateLocationReportPdf(
        context: Context,
        locations: List<LocationEntity>,
        outputStream: FileOutputStream,
        title: String = "位置跟踪报告"
    ): Boolean {
        return try {
            val pdfDocument = PdfDocument()
            
            // 计算需要的页数
            val itemsPerPage = 20 // 每页显示的项目数
            val totalPages = kotlin.math.ceil(locations.size.toDouble() / itemsPerPage).toInt()
            
            // 生成每一页
            for (pageNr in 0 until totalPages) {
                val page = pdfDocument.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNr + 1).create()
                )
                val canvas = page.canvas
                
                // 绘制页面内容
                drawPageContent(canvas, locations, pageNr, itemsPerPage, title, totalPages)
                
                pdfDocument.finishPage(page)
            }
            
            // 写入文件
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            
            Logger.i(TAG, "PDF report generated successfully with ${locations.size} locations")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate PDF report", e)
            false
        }
    }
    
    /**
     * 绘制页面内容
     */
    private fun drawPageContent(
        canvas: Canvas,
        locations: List<LocationEntity>,
        pageNumber: Int,
        itemsPerPage: Int,
        title: String,
        totalPages: Int
    ) {
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        // 绘制标题
        paint.textSize = 18f
        paint.color = Color.BLACK
        canvas.drawText(title, MARGIN.toFloat(), MARGIN.toFloat(), paint)
        
        // 绘制生成时间
        paint.textSize = 12f
        paint.color = Color.GRAY
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        canvas.drawText("生成时间: $timestamp", MARGIN.toFloat(), MARGIN + 30f, paint)
        
        // 绘制统计信息
        paint.color = Color.BLACK
        canvas.drawText("总计: ${locations.size} 个位置点", MARGIN.toFloat(), MARGIN + 50f, paint)
        
        // 绘制数据表格标题
        paint.textSize = 14f
        paint.color = Color.BLACK
        val startY = MARGIN + 80f
        val rowHeight = 20f
        
        // 表格列标题
        val headers = arrayOf("时间", "纬度", "经度", "精度", "海拔")
        val columnWidths = arrayOf(120f, 80f, 80f, 60f, 60f)
        
        // 绘制表头背景
        paint.color = Color.LTGRAY
        canvas.drawRect(
            MARGIN.toFloat(),
            startY,
            MARGIN + columnWidths.sum(),
            startY + rowHeight,
            paint
        )
        
        // 绘制表头文字
        paint.color = Color.BLACK
        var x = MARGIN.toFloat()
        for (i in headers.indices) {
            canvas.drawText(headers[i], x + 5, startY + 15, paint)
            x += columnWidths[i]
        }
        
        // 绘制数据行
        paint.textSize = 10f
        val startIndex = pageNumber * itemsPerPage
        val endIndex = kotlin.math.min(startIndex + itemsPerPage, locations.size)
        
        for (i in startIndex until endIndex) {
            val location = locations[i]
            val yPos = startY + rowHeight * (i - startIndex + 1) + 5
            
            // 绘制行背景（交替颜色）
            paint.color = if ((i - startIndex) % 2 == 0) Color.WHITE else Color.parseColor("#F5F5F5")
            canvas.drawRect(
                MARGIN.toFloat(),
                startY + rowHeight * (i - startIndex + 1),
                MARGIN + columnWidths.sum(),
                startY + rowHeight * (i - startIndex + 2),
                paint
            )
            
            // 绘制数据
            paint.color = Color.BLACK
            x = MARGIN.toFloat()
            
            // 时间
            val timeStr = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(location.timestamp))
            canvas.drawText(timeStr, x + 5, yPos, paint)
            x += columnWidths[0]
            
            // 纬度
            canvas.drawText(String.format("%.4f", location.latitude), x + 5, yPos, paint)
            x += columnWidths[1]
            
            // 经度
            canvas.drawText(String.format("%.4f", location.longitude), x + 5, yPos, paint)
            x += columnWidths[2]
            
            // 精度
            val accuracyStr = location.accuracy?.let { String.format("%.1f", it) } ?: "-"
            canvas.drawText(accuracyStr, x + 5, yPos, paint)
            x += columnWidths[3]
            
            // 海拔
            val altitudeStr = location.altitude?.let { String.format("%.1f", it) } ?: "-"
            canvas.drawText(altitudeStr, x + 5, yPos, paint)
        }
        
        // 绘制页码
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText(
            "第 ${pageNumber + 1} 页，共 $totalPages 页",
            MARGIN.toFloat(),
            PAGE_HEIGHT - 30f,
            paint
        )
        
        // 绘制页脚
        paint.textSize = 8f
        canvas.drawText(
            "由MQTT位置跟踪器生成",
            PAGE_WIDTH - MARGIN - 100f,
            PAGE_HEIGHT - 30f,
            paint
        )
    }
    
    /**
     * 生成位置轨迹图PDF
     */
    fun generateLocationMapPdf(
        context: Context,
        locations: List<LocationEntity>,
        outputStream: FileOutputStream
    ): Boolean {
        return try {
            val pdfDocument = PdfDocument()
            val page = pdfDocument.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            )
            val canvas = page.canvas
            
            // 绘制地图标题
            val paint = Paint().apply {
                isAntiAlias = true
                textSize = 18f
                color = Color.BLACK
            }
            
            canvas.drawText("位置轨迹图", MARGIN.toFloat(), MARGIN.toFloat(), paint)
            
            // 绘制生成时间
            paint.textSize = 12f
            paint.color = Color.GRAY
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            canvas.drawText("生成时间: $timestamp", MARGIN.toFloat(), MARGIN + 30f, paint)
            
            // 绘制统计信息
            paint.color = Color.BLACK
            canvas.drawText("总计: ${locations.size} 个位置点", MARGIN.toFloat(), MARGIN + 50f, paint)
            
            // 绘制简化的轨迹图（示意）
            if (locations.isNotEmpty()) {
                drawLocationPath(canvas, locations)
            }
            
            pdfDocument.finishPage(page)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            
            Logger.i(TAG, "Location map PDF generated successfully")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to generate location map PDF", e)
            false
        }
    }
    
    /**
     * 绘制位置轨迹路径
     */
    private fun drawLocationPath(canvas: Canvas, locations: List<LocationEntity>) {
        if (locations.size < 2) return
        
        val paint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 2f
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
        val mapTop = 150f
        val mapWidth = (PAGE_WIDTH - 2 * MARGIN).toFloat()
        val mapHeight = 300f
        
        // 绘制轨迹线
        val path = android.graphics.Path()
        var isFirst = true
        
        for (location in locations) {
            // 将经纬度转换为PDF坐标
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
            canvas.drawCircle(startX, startY, 5f, startPaint)
        }
        
        // 终点
        if (locations.isNotEmpty()) {
            val lastLoc = locations.last()
            val endX = mapLeft + ((lastLoc.longitude - minLng) / (maxLng - minLng)) * mapWidth
            val endY = mapTop + ((maxLat - lastLoc.latitude) / (maxLat - minLat)) * mapHeight
            canvas.drawCircle(endX, endY, 5f, endPaint)
        }
        
        // 添加图例
        val legendTop = mapTop + mapHeight + 20f
        canvas.drawCircle(MARGIN + 10f, legendTop, 5f, startPaint)
        canvas.drawText("起点", MARGIN + 20f, legendTop + 5f, paint)
        
        canvas.drawCircle(MARGIN + 80f, legendTop, 5f, endPaint)
        canvas.drawText("终点", MARGIN + 90f, legendTop + 5f, paint)
        
        canvas.drawCircle(MARGIN + 150f, legendTop, 3f, paint)
        canvas.drawText("轨迹", MARGIN + 160f, legendTop + 5f, paint)
    }
}