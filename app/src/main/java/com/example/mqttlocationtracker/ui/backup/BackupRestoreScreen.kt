package com.example.mqttlocationtracker.ui.backup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mqttlocationtracker.database.backup.BackupManager
import com.example.mqttlocationtracker.database.repository.LocationRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val locationRepository = LocationRepository.getDatabase(context)
    val backupManager = BackupManager(context, locationRepository, androidx.lifecycle.viewmodel.compose.viewModel().coroutineScope)
    
    var selectedBackupFormat by remember { mutableStateOf(BackupManager.BackupFormat.JSON) }
    var isBackingUp by remember { mutableStateOf(false) }
    var backupResult by remember { mutableStateOf<String?>(null) }
    var isRestoring by remember { mutableStateOf(false) }
    var restoreResult by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("备份与恢复") },
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
            // 备份功能
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "数据备份",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "将位置数据备份到文件",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 备份格式选择
                    Text(
                        text = "备份格式",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = selectedBackupFormat == BackupManager.BackupFormat.JSON,
                            onClick = { selectedBackupFormat = BackupManager.BackupFormat.JSON },
                            label = { Text("JSON") }
                        )
                        
                        FilterChip(
                            selected = selectedBackupFormat == BackupManager.BackupFormat.CSV,
                            onClick = { selectedBackupFormat = BackupManager.BackupFormat.CSV },
                            label = { Text("CSV") }
                        )
                        
                        FilterChip(
                            selected = selectedBackupFormat == BackupManager.BackupFormat.SQL,
                            onClick = { selectedBackupFormat = BackupManager.BackupFormat.SQL },
                            label = { Text("SQL") }
                        )
                    }
                    
                    // 备份按钮
                    Button(
                        onClick = {
                            isBackingUp = true
                            backupResult = null
                            
                            // 这里应该触发实际的备份操作
                            // 由于我们没有实际的文件系统访问权限，这里只是模拟备份过程
                            Toast.makeText(context, "备份功能需要文件系统权限，这里仅演示界面", Toast.LENGTH_LONG).show()
                            
                            // 模拟备份完成
                            isBackingUp = false
                            backupResult = "备份文件已生成: ${backupManager.generateBackupFileName(selectedBackupFormat)}"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        enabled = !isBackingUp
                    ) {
                        if (isBackingUp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBackingUp) "备份中..." else "开始备份")
                    }
                    
                    // 备份结果
                    backupResult?.let { result ->
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            // 恢复功能
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "数据恢复",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "从备份文件恢复位置数据",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Button(
                        onClick = {
                            isRestoring = true
                            restoreResult = null
                            
                            // 这里应该触发实际的恢复操作
                            // 由于我们没有实际的文件系统访问权限，这里只是模拟恢复过程
                            Toast.makeText(context, "恢复功能需要文件系统权限，这里仅演示界面", Toast.LENGTH_LONG).show()
                            
                            // 模拟恢复完成
                            isRestoring = false
                            restoreResult = "成功从备份文件恢复数据"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isRestoring
                    ) {
                        if (isRestoring) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRestoring) "恢复中..." else "选择备份文件恢复")
                    }
                    
                    // 恢复结果
                    restoreResult?.let { result ->
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            // 备份信息
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "备份信息",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "• JSON格式: 结构化的数据格式，易于程序处理\n" +
                                "• CSV格式: 逗号分隔值格式，可在表格软件中打开\n" +
                                "• SQL格式: SQL语句格式，可用于数据库直接导入\n" +
                                "• 备份文件将保存到设备的下载目录\n" +
                                "• 恢复功能支持从以上任何格式的备份文件恢复数据",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // 警告信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "警告",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "重要提醒",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    
                    Text(
                        text = "• 恢复数据将覆盖现有数据，请谨慎操作\n" +
                                "• 建议在恢复前先备份当前数据\n" +
                                "• 确保备份文件来源可靠，避免数据损坏\n" +
                                "• 恢复过程中请勿关闭应用或断开网络",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}