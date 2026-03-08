package com.example.mqttlocationtracker.ui.history

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryPlaybackScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val historyLocations by viewModel.historyLocations.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    
    // 动画播放相关状态
    var isPlaying by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var currentPosition by remember { mutableStateOf<LatLng?>(null) }
    var playbackLocations by remember { mutableStateListOf<LatLng>() }
    
    var cameraPositionState by rememberCameraPositionState {
        CameraPosition.fromLatLngZoom(
            LatLng(39.9042, 116.4074), // 默认北京位置
            10f
        )
    }
    
    // 加载历史数据
    LaunchedEffect(Unit) {
        viewModel.loadHistoryData()
    }
    
    // 显示错误消息
    errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    // 更新播放位置
    LaunchedEffect(isPlaying, historyLocations) {
        if (isPlaying && historyLocations.isNotEmpty()) {
            // 将历史位置转换为LatLng列表
            val positions = historyLocations.map { 
                LatLng(it.latitude, it.longitude) 
            }
            
            // 启动播放循环
            while (isPlaying && currentIndex < positions.size) {
                currentPosition = positions[currentIndex]
                playbackLocations.add(currentPosition!!)
                
                // 移动相机到当前位置
                currentPosition?.let { pos ->
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(pos, 15f)
                    )
                }
                
                currentIndex++
                
                // 根据播放速度延迟
                kotlinx.coroutines.delay((1000 / playbackSpeed).toLong())
            }
            
            // 播放完成后停止
            if (currentIndex >= positions.size) {
                isPlaying = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史轨迹回放") },
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
            // 地图视图
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    // 显示完整轨迹线
                    if (historyLocations.size > 1) {
                        val positions = historyLocations.map { 
                            LatLng(it.latitude, it.longitude) 
                        }
                        
                        Polyline(
                            points = positions,
                            color = android.graphics.Color.GRAY,
                            width = 3f
                        )
                    }
                    
                    // 显示已播放的轨迹线
                    if (playbackLocations.size > 1) {
                        Polyline(
                            points = playbackLocations.toList(),
                            color = android.graphics.Color.BLUE,
                            width = 5f
                        )
                    }
                    
                    // 显示当前位置标记
                    currentPosition?.let { pos ->
                        Marker(
                            state = MarkerState(position = pos),
                            title = "当前位置",
                            snippet = if (currentIndex <= historyLocations.size) {
                                "时间: ${SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(historyLocations[currentIndex-1].timestamp)}"
                            } else ""
                        )
                    }
                }
            }
            
            // 控制面板
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 进度条
                    if (historyLocations.isNotEmpty()) {
                        Text(
                            text = "进度: $currentIndex / ${historyLocations.size}",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LinearProgressIndicator(
                            progress = if (historyLocations.isEmpty()) 0f else currentIndex.toFloat() / historyLocations.size,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }
                    
                    // 控制按钮
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (historyLocations.isEmpty()) {
                                    Toast.makeText(context, "没有可用的历史数据", Toast.LENGTH_SHORT).show()
                                    return@IconButton
                                }
                                
                                isPlaying = !isPlaying
                                if (!isPlaying) {
                                    // 暂停时保持当前位置
                                } else {
                                    // 开始播放时如果已到达末尾则重置
                                    if (currentIndex >= historyLocations.size) {
                                        currentIndex = 0
                                        playbackLocations.clear()
                                    }
                                }
                            },
                            enabled = historyLocations.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "暂停" else "播放"
                            )
                        }
                        
                        Button(
                            onClick = {
                                // 重置播放
                                isPlaying = false
                                currentIndex = 0
                                currentPosition = null
                                playbackLocations.clear()
                                
                                // 重置相机位置
                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(39.9042, 116.4074), // 默认北京位置
                                        10f
                                    )
                                )
                            },
                            enabled = historyLocations.isNotEmpty()
                        ) {
                            Text("重置")
                        }
                    }
                    
                    // 速度控制
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text("播放速度:")
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = playbackSpeed,
                            onValueChange = { playbackSpeed = it },
                            valueRange = 0.5f..5.0f,
                            steps = 8,
                            modifier = Modifier.weight(1f)
                        )
                        Text("%.1fx".format(playbackSpeed))
                    }
                    
                    // 时间范围信息
                    if (historyLocations.isNotEmpty()) {
                        val firstTime = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(historyLocations.first().timestamp)
                        val lastTime = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(historyLocations.last().timestamp)
                        Text("时间范围: $firstTime ~ $lastTime")
                    }
                }
            }
        }
    }
}