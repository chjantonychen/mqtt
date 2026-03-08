package com.example.mqttlocationtracker.ui.history

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mqttlocationtracker.database.entity.LocationEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onNavigateToPlayback: () -> Unit,
    onNavigateToFilter: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val historyLocations by viewModel.historyLocations.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var isStartDateSelected by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadHistoryData()
    }

    errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史轨迹") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 日期选择区域
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "筛选条件",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                isStartDateSelected = true
                                showDatePicker = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column {
                                Text("开始日期")
                                Text(
                                    text = startDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) } ?: "未选择",
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                isStartDateSelected = false
                                showDatePicker = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column {
                                Text("结束日期")
                                Text(
                                    text = endDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) } ?: "未选择",
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (startDate != null && endDate != null) {
                                    if (startDate!!.after(endDate!!)) {
                                        Toast.makeText(context, "开始日期不能晚于结束日期", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.setDateRange(startDate!!, endDate!!)
                                    }
                                } else {
                                    Toast.makeText(context, "请选择完整的日期范围", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("筛选")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                startDate = null
                                endDate = null
                                viewModel.clearDateRange()
                            }
                        ) {
                            Text("清除")
                        }
                    }
                }
            }

            // 回放和过滤按钮
            if (!isLoading && historyLocations.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onNavigateToPlayback,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text("轨迹回放")
                    }
                    
                    OutlinedButton(
                        onClick = onNavigateToFilter,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text("数据过滤")
                    }
                }
            }
            
            // 历史数据列表
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (historyLocations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无历史数据")
                    }
                } else {
                    LazyColumn {
                        items(historyLocations) { location ->
                            LocationItem(location = location)
                        }
                    }
                }
            }
        }
    }

    // 日期选择对话框
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                
                if (isStartDateSelected) {
                    startDate = selectedDate
                } else {
                    endDate = selectedDate
                }
                
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

@Composable
fun LocationItem(location: LocationEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(location.timestamp),
                style = MaterialTheme.typography.subtitle1
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "纬度: ${location.latitude}",
                style = MaterialTheme.typography.body2
            )
            
            Text(
                text = "经度: ${location.longitude}",
                style = MaterialTheme.typography.body2
            )
            
            if (location.altitude != null) {
                Text(
                    text = "海拔: ${location.altitude}",
                    style = MaterialTheme.typography.body2
                )
            }
            
            if (location.accuracy != null) {
                Text(
                    text = "精度: ${location.accuracy}米",
                    style = MaterialTheme.typography.body2
                )
            }
            
            if (location.speed != null) {
                Text(
                    text = "速度: ${location.speed} m/s",
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}