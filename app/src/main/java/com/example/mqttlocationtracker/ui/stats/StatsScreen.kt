package com.example.mqttlocationtracker.ui.stats

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCharts: () -> Unit,
    viewModel: StatsViewModel = viewModel()
) {
    val context = LocalContext.current
    val statsData by viewModel.statsData.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()

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
                title = { Text("统计数据") },
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            statsData?.let { stats ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // 总览卡片
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "总览",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            StatItem(
                                label = "总位置数",
                                value = stats.totalLocations.toString()
                            )

                            StatItem(
                                label = "今日位置数",
                                value = stats.todayLocations.toString()
                            )

                            StatItem(
                                label = "待同步位置数",
                                value = stats.unsyncedLocations.toString()
                            )
                        }
                    }

                    // 距离统计卡片
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "距离统计",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            StatItem(
                                label = "总距离",
                                value = viewModel.formatDistance(stats.totalDistance)
                            )

                            stats.averageAccuracy?.let { accuracy ->
                                StatItem(
                                    label = "平均精度",
                                    value = "${String.format("%.1f", accuracy)} 米"
                                )
                            }
                        }
                    }

                    // 时间统计卡片
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "时间统计",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            StatItem(
                                label = "首次记录",
                                value = viewModel.formatDate(stats.firstLocationTime)
                            )

                            StatItem(
                                label = "最后记录",
                                value = viewModel.formatDate(stats.lastLocationTime)
                            )
                        }
                    }

                    // 刷新按钮
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { viewModel.loadStatsData() },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        ) {
                            Text("刷新数据")
                        }
                        
                        OutlinedButton(
                            onClick = onNavigateToCharts,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.BarChart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("查看图表")
                        }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无统计数据")
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}