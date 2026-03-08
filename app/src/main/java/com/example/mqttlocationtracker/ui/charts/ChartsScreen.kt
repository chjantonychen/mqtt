package com.example.mqttlocationtracker.ui.charts

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mqttlocationtracker.ui.stats.StatsViewModel
import com.example.mqttlocationtracker.utils.ChartHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

@Composable
fun ChartsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = viewModel()
) {
    val context = LocalContext.current
    val statsData by viewModel.statsData.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    
    var chartType by remember { mutableStateOf(ChartType.DAILY_COUNTS) }
    
    // 加载统计数据
    LaunchedEffect(Unit) {
        viewModel.loadStatsData()
    }
    
    // 显示错误消息
    errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据统计图表") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 图表类型选择
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "图表类型",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = chartType == ChartType.DAILY_COUNTS,
                            onClick = { chartType = ChartType.DAILY_COUNTS },
                            label = { Text("每日统计") }
                        )
                        
                        FilterChip(
                            selected = chartType == ChartType.HOURLY_COUNTS,
                            onClick = { chartType = ChartType.HOURLY_COUNTS },
                            label = { Text("每小时统计") }
                        )
                        
                        FilterChip(
                            selected = chartType == ChartType.ACCURACY_DISTRIBUTION,
                            onClick = { chartType = ChartType.ACCURACY_DISTRIBUTION },
                            label = { Text("精度分布") }
                        )
                    }
                }
            }
            
            // 图表显示区域
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                statsData?.let { stats ->
                    // 生成图表数据
                    val chartData = when (chartType) {
                        ChartType.DAILY_COUNTS -> ChartHelper.generateDailyLocationCounts(stats.allLocations)
                        ChartType.HOURLY_COUNTS -> ChartHelper.generateHourlyLocationCounts(stats.allLocations)
                        ChartType.ACCURACY_DISTRIBUTION -> ChartHelper.generateAccuracyDistribution(stats.allLocations)
                    }
                    
                    if (chartData.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无数据可显示")
                        }
                    } else {
                        // 显示图表
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            // 图表标题
                            Text(
                                text = getChartTitle(chartType),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
                            
                            // 图表
                            ChartView(
                                dataPoints = chartData,
                                chartType = chartType,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无统计数据")
                    }
                }
            }
            
            // 基本统计信息
            statsData?.let { stats ->
                val basicStats = ChartHelper.calculateBasicStatistics(stats.allLocations)
                if (basicStats.totalLocations > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "基本统计",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = "总位置数: ${basicStats.totalLocations}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            
                            Text(
                                text = "数据范围: ${basicStats.formatDateRange()} (${basicStats.dateRangeDays}天)",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            
                            basicStats.averageAccuracy?.let {
                                Text(
                                    text = "平均精度: ${String.format("%.1f", it)}米",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            
                            basicStats.averageSpeed?.let {
                                Text(
                                    text = "平均速度: ${String.format("%.2f", it)} m/s",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 图表视图
 */
@Composable
fun ChartView(
    dataPoints: List<ChartHelper.ChartDataPoint>,
    chartType: ChartType,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("暂无数据")
        }
        return
    }
    
    // 根据图表类型选择渲染方式
    when (chartType) {
        ChartType.DAILY_COUNTS, ChartType.HOURLY_COUNTS -> {
            BarChartView(dataPoints = dataPoints, modifier = modifier)
        }
        ChartType.ACCURACY_DISTRIBUTION -> {
            PieChartView(dataPoints = dataPoints, modifier = modifier)
        }
    }
}

/**
 * 柱状图视图
 */
@Composable
fun BarChartView(
    dataPoints: List<ChartHelper.ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    val maxValue = dataPoints.maxOfOrNull { it.value } ?: 1.0
    val barColor = Color(0xFF6200EE)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Canvas(modifier = modifier) { 
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 20f
        val chartWidth = canvasWidth - 2 * padding
        val chartHeight = canvasHeight - 2 * padding
        
        // 计算柱状图参数
        val barCount = dataPoints.size
        val barWidth = if (barCount > 0) (chartWidth / barCount) * 0.8f else 0f
        val spacing = if (barCount > 0) (chartWidth / barCount) * 0.2f else 0f
        
        // 绘制柱状图
        dataPoints.forEachIndexed { index, point ->
            val barHeight = (point.value / maxValue).toFloat() * chartHeight
            val left = padding + index * (barWidth + spacing)
            val top = canvasHeight - padding - barHeight
            val right = left + barWidth
            val bottom = canvasHeight - padding
            
            // 绘制柱子
            drawRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
            
            // 绘制标签（如果空间足够）
            if (barWidth > 20f) {
                val label = if (point.label.length > 5) {
                    point.label.take(5) + "..."
                } else {
                    point.label
                }
                
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    left + barWidth / 2,
                    canvasHeight - 5,
                    android.graphics.Paint().apply {
                        textSize = 24f
                        color = labelColor.hashCode()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
        
        // 绘制Y轴标签
        val yAxisLabels = listOf("0", (maxValue * 0.5).toInt().toString(), maxValue.toInt().toString())
        yAxisLabels.forEachIndexed { index, label ->
            val y = canvasHeight - padding - (index * chartHeight / 2)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                10f,
                y + 10,
                android.graphics.Paint().apply {
                    textSize = 24f
                    color = labelColor.hashCode()
                }
            )
        }
    }
}

/**
 * 饼图视图
 */
@Composable
fun PieChartView(
    dataPoints: List<ChartHelper.ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    val totalValue = dataPoints.sumOf { it.value }
    val colors = listOf(
        Color(0xFF6200EE),
        Color(0xFF03DAC6),
        Color(0xFFFFA000),
        Color(0xFF7B1FA2),
        Color(0xFF388E3C),
        Color(0xFFD32F2F)
    )
    
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val radius = minOf(canvasWidth, canvasHeight) / 3
        
        var currentAngle = 0f
        
        // 绘制饼图扇形
        dataPoints.forEachIndexed { index, point ->
            val sweepAngle = (point.value / totalValue * 360).toFloat()
            val color = colors[index % colors.size]
            
            drawArc(
                color = color,
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
            
            currentAngle += sweepAngle
        }
        
        // 绘制图例
        val legendPadding = 20f
        val legendItemHeight = 40f
        val legendStartY = legendPadding
        
        dataPoints.forEachIndexed { index, point ->
            val y = legendStartY + index * legendItemHeight
            val color = colors[index % colors.size]
            
            // 绘制颜色方块
            drawRect(
                color = color,
                topLeft = Offset(canvasWidth - 150, y),
                size = androidx.compose.ui.geometry.Size(20f, 20f)
            )
            
            // 绘制标签
            drawContext.canvas.nativeCanvas.drawText(
                "${point.label}: ${point.value.toInt()}",
                canvasWidth - 120,
                y + 15,
                android.graphics.Paint().apply {
                    textSize = 32f
                    color = androidx.compose.ui.graphics.Color.Black.hashCode()
                }
            )
        }
    }
}

/**
 * 获取图表标题
 */
private fun getChartTitle(chartType: ChartType): String {
    return when (chartType) {
        ChartType.DAILY_COUNTS -> "每日位置数量统计"
        ChartType.HOURLY_COUNTS -> "每小时位置数量统计"
        ChartType.ACCURACY_DISTRIBUTION -> "位置精度分布"
    }
}

/**
 * 图表类型枚举
 */
enum class ChartType {
    DAILY_COUNTS,
    HOURLY_COUNTS,
    ACCURACY_DISTRIBUTION
}