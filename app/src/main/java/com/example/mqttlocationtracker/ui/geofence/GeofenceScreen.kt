package com.example.mqttlocationtracker.ui.geofence

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mqttlocationtracker.geofence.GeofenceManager
import java.util.*

@Composable
fun GeofenceScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val geofenceManager = remember { GeofenceManager(context) }
    val geofences = remember { mutableStateListOf<GeofenceManager.GeofenceData>() }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var newGeofenceName by remember { mutableStateOf("") }
    var newGeofenceLatitude by remember { mutableStateOf("") }
    var newGeofenceLongitude by remember { mutableStateOf("") }
    var newGeofenceRadius by remember { mutableStateOf("100") }
    var newGeofenceDescription by remember { mutableStateOf("") }
    
    // 加载现有的地理围栏
    LaunchedEffect(Unit) {
        geofences.addAll(geofenceManager.getAllGeofences())
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("地理围栏") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "添加围栏")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (geofences.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无地理围栏")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    items(geofences) { geofence ->
                        GeofenceItem(
                            geofence = geofence,
                            onDelete = {
                                geofenceManager.removeGeofence(geofence.id)
                                geofences.remove(geofence)
                                Toast.makeText(context, "已删除围栏: ${geofence.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
            
            // 说明信息
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
                        text = "使用说明",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• 点击右下角的+按钮添加新的地理围栏\n" +
                                "• 输入围栏的名称、坐标和半径\n" +
                                "• 当设备进入或离开围栏区域时会收到通知\n" +
                                "• 可以通过滑动删除按钮移除不需要的围栏",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
    
    // 添加地理围栏对话框
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加地理围栏") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newGeofenceName,
                        onValueChange = { newGeofenceName = it },
                        label = { Text("围栏名称") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = newGeofenceLatitude,
                        onValueChange = { newGeofenceLatitude = it },
                        label = { Text("纬度") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = newGeofenceLongitude,
                        onValueChange = { newGeofenceLongitude = it },
                        label = { Text("经度") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = newGeofenceRadius,
                        onValueChange = { newGeofenceRadius = it },
                        label = { Text("半径(米)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = newGeofenceDescription,
                        onValueChange = { newGeofenceDescription = it },
                        label = { Text("描述(可选)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (validateGeofenceInput(
                                newGeofenceName,
                                newGeofenceLatitude,
                                newGeofenceLongitude,
                                newGeofenceRadius
                            )) {
                            val geofenceData = GeofenceManager.GeofenceData(
                                id = UUID.randomUUID().toString(),
                                name = newGeofenceName,
                                latitude = newGeofenceLatitude.toDouble(),
                                longitude = newGeofenceLongitude.toDouble(),
                                radius = newGeofenceRadius.toFloat(),
                                description = newGeofenceDescription
                            )
                            
                            if (geofenceManager.addGeofence(geofenceData)) {
                                geofences.add(geofenceData)
                                Toast.makeText(context, "已添加围栏: ${geofenceData.name}", Toast.LENGTH_SHORT).show()
                                
                                // 清空输入
                                newGeofenceName = ""
                                newGeofenceLatitude = ""
                                newGeofenceLongitude = ""
                                newGeofenceRadius = "100"
                                newGeofenceDescription = ""
                                showAddDialog = false
                            } else {
                                Toast.makeText(context, "添加围栏失败", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "请输入有效的围栏信息", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun GeofenceItem(
    geofence: GeofenceManager.GeofenceData,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = geofence.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (geofence.description.isNotEmpty()) {
                    Text(
                        text = geofence.description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Text(
                    text = "坐标: ${String.format("%.4f", geofence.latitude)}, ${String.format("%.4f", geofence.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Text(
                    text = "半径: ${geofence.radius}米",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除"
                )
            }
        }
    }
}

/**
 * 验证地理围栏输入
 */
private fun validateGeofenceInput(
    name: String,
    latitude: String,
    longitude: String,
    radius: String
): Boolean {
    if (name.isBlank()) return false
    
    try {
        val lat = latitude.toDouble()
        val lng = longitude.toDouble()
        val rad = radius.toFloat()
        
        // 验证纬度范围
        if (lat < -90 || lat > 90) return false
        
        // 验证经度范围
        if (lng < -180 || lng > 180) return false
        
        // 验证半径范围
        if (rad <= 0 || rad > 10000) return false
        
        return true
    } catch (e: NumberFormatException) {
        return false
    }
}