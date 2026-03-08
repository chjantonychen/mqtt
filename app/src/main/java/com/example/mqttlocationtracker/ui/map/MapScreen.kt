package com.example.mqttlocationtracker.ui.map

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.example.mqttlocationtracker.utils.LocationClusteringHelper
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 地图界面
 */
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val mapLocations by viewModel.mapLocations.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    
    var cameraPositionState by rememberCameraPositionState {
        CameraPosition.fromLatLngZoom(
            LatLng(39.9042, 116.4074), // 默认北京位置
            10f
        )
    }
    
    // 计算聚类结果
    val clusters by remember(mapLocations, cameraPositionState.position.zoom) {
        derivedStateOf {
            if (mapLocations.isEmpty()) {
                emptyList()
            } else {
                // 获取屏幕尺寸
                val screenWidth = with(density) { 1080.dp.toPx() }.toInt() // 默认宽度
                val screenHeight = with(density) { 1920.dp.toPx() }.toInt() // 默认高度
                
                LocationClusteringHelper.clusterLocations(
                    locations = mapLocations,
                    zoomLevel = cameraPositionState.position.zoom,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight
                )
            }
        }
    }
    
    // 加载地图数据
    LaunchedEffect(Unit) {
        viewModel.loadMapData()
    }
    
    // 显示错误消息
    errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("位置地图") },
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
                cameraPositionState = cameraPositionState
            ) {
                // 显示历史轨迹线
                if (mapLocations.size > 1) {
                    val positions = mapLocations.map { 
                        LatLng(it.latitude, it.longitude) 
                    }
                    
                    Polyline(
                        points = positions,
                        color = android.graphics.Color.BLUE,
                        width = 5f
                    )
                }
                
                // 显示聚类标记点
                clusters.forEach { cluster ->
                    Marker(
                        state = MarkerState(position = cluster.position),
                        title = cluster.title,
                        snippet = cluster.description
                    )
                }
            }
            
            // 地图控制按钮
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                // 刷新按钮
                Button(
                    onClick = { viewModel.loadMapData() },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        Text("加载中...")
                    } else {
                        Text("刷新")
                    }
                }
                
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
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text("显示全部轨迹")
                }
                
                OutlinedButton(
                    onClick = {
                        // 清除所有标记
                        if (mapLocations.isNotEmpty()) {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(39.9042, 116.4074), // 默认北京位置
                                    10f
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("重置视图")
                }
            }
        }
    }
}