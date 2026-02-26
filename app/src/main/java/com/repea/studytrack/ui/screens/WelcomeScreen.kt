package com.repea.studytrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.ui.navigation.Screen

@Composable
fun WelcomeScreen(
    navController: NavController,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "欢迎使用 学迹",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "StudyTrack",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureItem("📚 科目管理", "轻松管理你的所有学习科目", color = MaterialTheme.colorScheme.onSurface)
                FeatureItem("📝 成绩记录", "详细记录每次考试成绩与排名", color = MaterialTheme.colorScheme.onSurface)
                FeatureItem("📈 趋势分析", "可视化图表展示进步曲线", color = MaterialTheme.colorScheme.onSurface)
                FeatureItem("📤 数据导出", "支持 Excel 导入导出备份", color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                onFinish()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Welcome.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("开启学习之旅", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun FeatureItem(title: String, desc: String, color: Color) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, color = color)
        Text(desc, style = MaterialTheme.typography.bodyMedium, color = color.copy(alpha = 0.78f))
    }
}
