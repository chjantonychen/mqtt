package com.example.mqttlocationtracker.ui.heatmap

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mqttlocationtracker.ui.map.MapViewModel
import com.example.mqttlocationtracker.utils.HeatmapHelper
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider

@Composable
fun HeatmapScreen(
    onNavigateBack: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val mapLocations by viewModel.mapLocations.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    
    var cameraPositionState by rememberCameraPositionState {
        CameraPosition.fromLatLngZoom(
            LatLng(39.9042, 116.4074), // 默认北京位置
            10f
        )
    }
    
    var intensityMode by remember { mutableStateOf(HeatmapHelper.IntensityMode.FREQUENCY) }
    var showControls by remember { mutableStateOf(true) }
    
    var heatmapTileProvider by remember { mutableStateOf<HeatmapTileProvider?>(null) }
    var heatmapGradient by remember { mutableStateOf(Gradient.DEFAULT) }
    
    // 加载地图数据
    LaunchedEffect(Unit) {
        viewModel.loadMapData()
    }
    
    // 显示错误消息
    errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    // 更新热力图数据
    LaunchedEffect(mapLocations, intensityMode) {
        if (mapLocations.isNotEmpty()) {
            try {
                val weightedLocations = HeatmapHelper.generateWeightedLocations(mapLocations, intensityMode)
                
                heatmapTileProvider = HeatmapTileProvider.Builder()
                    .weightedData(weightedLocations)
                    .gradient(heatmapGradient)
                    .build()
            } catch (e: Exception) {
                Toast.makeText(context, "生成热力图失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("位置热力图") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Google地图
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { showControls = !showControls }
            ) {
                // 显示热力图层
                heatmapTileProvider?.let { provider ->
                    val radius = HeatmapHelper.calculateHeatmapRadius(cameraPositionState.position.zoom)
                    val opacity = HeatmapHelper.calculateHeatmapOpacity(cameraPositionState.position.zoom)
                    
                    provider.setRadius(radius.toInt())
                    provider.setOpacity(opacity)
                    
                    TileOverlay(
                        tileProvider = provider
                    )
                }
            }
            
            // 控制面板
            if (showControls) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    // 强度模式选择
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        elevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "热力图模式",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FilterChip(
                                    selected = intensityMode == HeatmapHelper.IntensityMode.FREQUENCY,
                                    onClick = { intensityMode = HeatmapHelper.IntensityMode.FREQUENCY },
                                    label = { Text("访问频率") }
                                )
                                
                                FilterChip(
                                    selected = intensityMode == HeatmapHelper.IntensityMode.ACCURACY,
                                    onClick = { intensityMode = HeatmapHelper.IntensityMode.ACCURACY },
                                    label = { Text("位置精度") }
                                )
                                
                                FilterChip(
                                    selected = intensityMode == HeatmapHelper.IntensityMode.SPEED,
                                    onClick = { intensityMode = HeatmapHelper.IntensityMode.SPEED },
                                    label = { Text("移动速度") }
                                )
                            }
                        }
                    }
                    
                    // 缩放控制
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "地图控制",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Button(
                                onClick = {
                                    // 缩放到适合所有标记的视图
                                    if (mapLocations.isNotEmpty()) {
                                        val bounds = mapLocations.map { 
                                            LatLng(it.latitude, it.longitude) 
                                        }.let { positions ->
                                            LatLngBounds.builder().apply {
                                                positions.forEach { include(it) }
                                            }.build()
                                        }
                                        
                                        cameraPositionState.move(
                                            CameraUpdateFactory.newLatLngBounds(bounds, 100)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("显示全部数据")
                            }
                        }
                    }
                }
            }
            
            // 加载指示器
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Card(
                        elevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text("加载数据中...")
                        }
                    }
                }
            }
            
            // 数据统计
            if (!isLoading && mapLocations.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "数据统计",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "位置点: ${mapLocations.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "模式: ${getIntensityModeLabel(intensityMode)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 获取强度模式标签
 */
private fun getIntensityModeLabel(mode: HeatmapHelper.IntensityMode): String {
    return when (mode) {
        HeatmapHelper.IntensityMode.FREQUENCY -> "访问频率"
        HeatmapHelper.IntensityMode.ACCURACY -> "位置精度"
        HeatmapHelper.IntensityMode.SPEED -> "移动速度"
    }
}