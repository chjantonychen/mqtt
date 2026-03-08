package com.example.mqttlocationtracker.ui.filter

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mqttlocationtracker.database.entity.LocationEntity
import com.example.mqttlocationtracker.ui.history.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FilterScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val historyLocations by viewModel.historyLocations.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()

    // 过滤条件状态
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var maxAccuracy by remember { mutableStateOf<Float?>(null) }
    var minAccuracyText by remember { mutableStateOf("") }

    // 应用的过滤条件
    var appliedFilters by remember { mutableStateOf<FilterCriteria?>(null) }

    // 显示错误消息
    errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据过滤") },
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
            // 过滤条件面板
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
                        text = "过滤条件",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // 时间范围过滤
                    Text(
                        text = "时间范围",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { showStartDatePicker = true },
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
                            onClick = { showEndDatePicker = true },
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

                    // 精度过滤
                    Text(
                        text = "精度过滤",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = minAccuracyText,
                        onValueChange = { 
                            minAccuracyText = it
                            maxAccuracy = it.toFloatOrNull()
                        },
                        label = { Text("最大精度(米，留空不过滤)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 操作按钮
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                startDate = null
                                endDate = null
                                maxAccuracy = null
                                minAccuracyText = ""
                                appliedFilters = null
                                viewModel.clearDateRange()
                            }
                        ) {
                            Text("清除条件")
                        }

                        Button(
                            onClick = {
                                val filters = FilterCriteria(
                                    startDate = startDate,
                                    endDate = endDate,
                                    maxAccuracy = maxAccuracy
                                )
                                appliedFilters = filters
                                
                                // 应用过滤条件
                                applyFilters(viewModel, filters)
                            }
                        ) {
                            Text("应用过滤")
                        }
                    }

                    // 已应用的过滤条件
                    appliedFilters?.let { filters ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text(
                                text = "已应用的过滤条件:",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            filters.startDate?.let {
                                Text(
                                    text = "开始日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            filters.endDate?.let {
                                Text(
                                    text = "结束日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            filters.maxAccuracy?.let {
                                Text(
                                    text = "最大精度: ${it}米",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // 过滤结果
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
                        Text("暂无符合条件的数据")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Text(
                                text = "找到 ${historyLocations.size} 条符合条件的记录",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                        }
                        
                        items(historyLocations) { location ->
                            LocationItem(location = location)
                        }
                    }
                }
            }
        }
    }

    // 日期选择对话框
    if (showStartDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                startDate = selectedDate
                showStartDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showEndDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                endDate = selectedDate
                showEndDatePicker = false
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
            
            if (location.accuracy != null) {
                Text(
                    text = "精度: ${location.accuracy}米",
                    style = MaterialTheme.typography.body2
                )
            }
            
            if (location.altitude != null) {
                Text(
                    text = "海拔: ${location.altitude}",
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

/**
 * 过滤条件数据类
 */
data class FilterCriteria(
    val startDate: Date?,
    val endDate: Date?,
    val maxAccuracy: Float?
)

/**
 * 应用过滤条件
 */
private fun applyFilters(viewModel: HistoryViewModel, filters: FilterCriteria) {
    // 目前我们只实现了时间范围过滤
    // 精度过滤需要在Repository层实现
    if (filters.startDate != null && filters.endDate != null) {
        viewModel.setDateRange(filters.startDate, filters.endDate)
    } else {
        viewModel.loadHistoryData()
    }
}