package com.example.mqttlocationtracker.ui.data

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
import com.example.mqttlocationtracker.ui.export.ExportViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DataManagementScreen(
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
                title = { Text("数据管理") },
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
            // 导出数据部分
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "导出数据",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // 导出格式选择
                    Text(
                        text = "导出格式",
                        style = MaterialTheme.typography.titleMedium,
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
                        
                        FilterChip(
                            selected = selectedFormat == "KML",
                            onClick = { selectedFormat = "KML" },
                            label = { Text("KML") }
                        )
                        
                        FilterChip(
                            selected = selectedFormat == "GPX",
                            onClick = { selectedFormat = "GPX" },
                            label = { Text("GPX") }
                        )
                        
                        FilterChip(
                            selected = selectedFormat == "Excel",
                            onClick = { selectedFormat = "Excel" },
                            label = { Text("Excel") }
                        )
                        
                        FilterChip(
                            selected = selectedFormat == "PDF",
                            onClick = { selectedFormat = "PDF" },
                            label = { Text("PDF") }
                        )
                        
                        FilterChip(
                            selected = selectedFormat == "Image",
                            onClick = { selectedFormat = "Image" },
                            label = { Text("图片") }
                        )
                        
                        FilterChip(
                            selected = selectedFormat == "ZIP",
                            onClick = { selectedFormat = "ZIP" },
                            label = { Text("ZIP压缩包") }
                        )
                        
                        FilterChip(
                            selected = selectedFormat == "Database",
                            onClick = { selectedFormat = "Database" },
                            label = { Text("数据库") }
                        )
                        
                        FilterChip(
                            selected = selectedFormat == "HTML",
                            onClick = { selectedFormat = "HTML" },
                            label = { Text("HTML报告") }
                        )
                        
                        FilterChip(
                            selected = selectedFormat == "Markdown",
                            onClick = { selectedFormat = "Markdown" },
                            label = { Text("Markdown") }
                        )
                        
                        FilterChip(
                            selected = selectedFormat == "XML",
                            onClick = { selectedFormat = "XML" },
                            label = { Text("XML") }
                        )
                        
                        FilterChip(
                            selected = selectedFormat == "YAML",
                            onClick = { selectedFormat = "YAML" },
                            label = { Text("YAML") }
                        )
                        
                        FilterChip(
                            selected = selectedFormat == "GeoJSON",
                            onClick = { selectedFormat = "GeoJSON" },
                            label = { Text("GeoJSON") }
                        )
                        
                        FilterChip(
                            selected = selectedFormat == "Shapefile",
                            onClick = { selectedFormat = "Shapefile" },
                            label = { Text("Shapefile") }
                        )
                    }

                    // 日期范围选择
                    Text(
                        text = "时间范围",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
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
                            .padding(top = 16.dp),
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
                                    .padding(top = 16.dp)
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
                }
            }

            // 导入数据部分
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "导入数据",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "从备份文件导入位置数据",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            // 这里应该触发实际的导入操作
                            Toast.makeText(context, "导入功能需要文件系统权限，这里仅演示界面", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("选择备份文件导入")
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
                                "• KML格式: Keyhole标记语言文件，可在Google Earth等地图软件中打开\n" +
                                "• GPX格式: GPS交换格式文件，可在各种GPS设备和软件中使用\n" +
                                "• Excel格式: Excel兼容的CSV文件，专为Microsoft Excel优化\n" +
                                "• PDF格式: 便携式文档格式文件，适合打印和分享\n" +
                                "• 图片格式: PNG/JPEG格式的位置轨迹图，便于视觉展示\n" +
                                "• ZIP压缩包: 包含多种格式的压缩文件，方便批量传输\n" +
                                "• 数据库格式: SQLite数据库文件，便于专业数据处理\n" +
                                "• HTML报告: 交互式网页报告，便于浏览和分享\n" +
                                "• Markdown格式: 轻量级标记语言报告，适合技术文档\n" +
                                "• XML格式: 可扩展标记语言文件，适合系统间数据交换\n" +
                                "• YAML格式: 人类可读的数据序列化标准，适合配置文件\n" +
                                "• GeoJSON格式: 地理空间数据交换格式，适合GIS应用\n" +
                                "• Shapefile格式: ESRI Shapefile格式，专业GIS数据格式\n" +
                                "• 选择时间范围可以只导出特定时间段的数据\n" +
                                "• 导出的文件将保存到设备的下载目录\n" +
                                "• 导入功能支持从备份文件恢复位置数据",
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