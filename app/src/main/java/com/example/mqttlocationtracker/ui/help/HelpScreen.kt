package com.example.mqttlocationtracker.ui.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("使用帮助") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // 使用步骤
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "使用步骤",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    HelpStep(
                        step = 1,
                        title = "配置MQTT连接",
                        description = "进入设置页面，配置MQTT服务器地址、客户端ID、用户名、密码和主题。"
                    )

                    HelpStep(
                        step = 2,
                        title = "连接MQTT服务器",
                        description = "在主页面点击\"连接\"按钮，连接到配置的MQTT服务器。"
                    )

                    HelpStep(
                        step = 3,
                        title = "开始位置跟踪",
                        description = "确保已连接MQTT服务器，然后点击\"开始跟踪\"按钮开始位置跟踪。"
                    )

                    HelpStep(
                        step = 4,
                        title = "查看实时位置",
                        description = "在主页面可以看到当前的实时位置信息。"
                    )

                    HelpStep(
                        step = 5,
                        title = "查看历史数据",
                        description = "点击\"查看历史位置\"可以查看历史位置记录，或点击\"查看地图\"在地图上显示轨迹。"
                    )
                }
            }

            // 功能说明
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "功能说明",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    HelpFeature(
                        title = "实时位置跟踪",
                        description = "应用可以在后台持续跟踪设备位置，并将位置数据发送到MQTT服务器。"
                    )

                    HelpFeature(
                        title = "离线数据存储",
                        description = "当网络不可用时，位置数据会保存在本地数据库中，网络恢复后自动同步。"
                    )

                    HelpFeature(
                        title = "历史轨迹回放",
                        description = "可以查看历史位置数据，并在地图上以动画形式回放轨迹。"
                    )

                    HelpFeature(
                        title = "数据导出",
                        description = "支持将位置数据导出为CSV或JSON格式文件。"
                    )

                    HelpFeature(
                        title = "统计数据",
                        description = "提供位置数据的统计信息，包括总距离、记录数量等。"
                    )
                }
            }

            // 注意事项
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "注意事项",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    HelpNote(
                        title = "电池优化",
                        description = "为了确保后台位置跟踪正常工作，请将此应用添加到电池优化白名单。"
                    )

                    HelpNote(
                        title = "位置权限",
                        description = "应用需要位置权限才能获取设备位置，请确保已授予相关权限。"
                    )

                    HelpNote(
                        title = "网络连接",
                        description = "位置数据通过MQTT协议发送，需要稳定的网络连接以确保数据传输。"
                    )

                    HelpNote(
                        title = "数据存储",
                        description = "应用会定期清理旧的历史数据以节省存储空间，默认保留30天的数据。"
                    )
                }
            }

            // 故障排除
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "故障排除",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    HelpTroubleshoot(
                        problem = "无法连接MQTT服务器",
                        solution = "检查网络连接、服务器地址、端口和认证信息是否正确。"
                    )

                    HelpTroubleshoot(
                        problem = "位置不准确",
                        solution = "确保在开阔区域使用，并检查GPS信号强度。"
                    )

                    HelpTroubleshoot(
                        problem = "后台跟踪停止",
                        solution = "检查应用是否被系统杀死，或将应用添加到后台运行白名单。"
                    )

                    HelpTroubleshoot(
                        problem = "数据不同步",
                        solution = "检查网络连接状态，应用会在网络恢复后自动同步数据。"
                    )
                }
            }
        }
    }
}

@Composable
fun HelpStep(step: Int, title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 32.dp, top = 4.dp)
        )
    }
}

@Composable
fun HelpFeature(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun HelpNote(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun HelpTroubleshoot(problem: String, solution: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = problem,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "解决方案: $solution",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}