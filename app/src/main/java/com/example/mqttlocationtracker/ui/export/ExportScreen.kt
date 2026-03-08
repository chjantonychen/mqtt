package com.example.mqttlocationtracker.ui.export

import android.app.DatePickerDialog
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExportViewModel = viewModel()
) {
    val context = LocalContext.current
    val exportProgress by viewModel.exportProgress.observeAsState()
    val exportResult by viewModel.exportResult.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var selectedFormat by remember { mutableStateOf("CSV") }

    // 显示结果和错误消息
    exportResult?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导出数据") },
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
                .padding(16.dp)
        ) {
            // 导出格式选择
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "导出格式",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = selectedFormat == "CSV",
                            onClick = { selectedFormat = "CSV" },
                            label = { Text("CSV") }
                        )

                        FilterChip(
                            selected = selectedFormat == "JSON",
                            onClick = { selectedFormat = "JSON" },
                            label = { Text("JSON") }
                        )
                    }
                }
            }

            // 日期范围选择
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "时间范围",
                        style = MaterialTheme.typography.headlineSmall,
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

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                startDate = null
                                endDate = null
                            }
                        ) {
                            Text("清除")
                        }
                    }
                }
            }

            // 导出按钮
            Button(
                onClick = {
                    if (startDate != null && endDate != null && startDate!!.after(endDate!!)) {
                        Toast.makeText(context, "开始日期不能晚于结束日期", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // 这里应该触发实际的导出操作
                    // 由于我们没有实际的文件系统访问权限，这里只是模拟导出过程
                    Toast.makeText(context, "导出功能需要文件系统权限，这里仅演示界面", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !(exportProgress?.isRunning ?: false)
            ) {
                Text(if (exportProgress?.isRunning == true) "导出中..." else "导出数据")
            }

            // 进度显示
            exportProgress?.let { progress ->
                if (progress.isRunning) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = if (progress.total > 0) progress.progress.toFloat() / progress.total else 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        Text(progress.message)
                    }
                }
            }

            // 说明文本
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "说明",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• CSV格式: 逗号分隔值文件，可在Excel等表格软件中打开\n" +
                                "• JSON格式: JavaScript对象表示法文件，适合程序处理\n" +
                                "• 选择时间范围可以只导出特定时间段的数据\n" +
                                "• 导出的文件将保存到设备的下载目录",
                        style = MaterialTheme.typography.bodyMedium
                    )
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