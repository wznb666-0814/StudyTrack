package com.repea.studytrack.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
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
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    (slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(animationSpec = tween(300)))
                        .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut(animationSpec = tween(300)))
                },
                label = "welcome_page"
            ) { page ->
                when (page) {
                    0 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FeatureItem("🌈 沉浸式界面", "壁纸 + 液态玻璃 + 自适应文字颜色，让学习记录也可以很好看。", color = MaterialTheme.colorScheme.onSurface)
                            FeatureItem("🔐 本地隐私", "所有数据仅保存在本机，不上传服务器，更安心。", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FeatureItem("📚 科目管理", "自定义科目名称与满分分值，一键管理全部学科。", color = MaterialTheme.colorScheme.onSurface)
                            FeatureItem("📝 成绩记录", "记录分数、班排、年排、区排与反思，还可以添加配图。", color = MaterialTheme.colorScheme.onSurface)
                            FeatureItem("📈 成绩与排名趋势", "按科目和自选科目总分查看折线图，直观对比每次考试。", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FeatureItem("📊 Excel 导入导出", "支持按模板从 Excel 批量导入 / 导出成绩，方便备份与迁移。", color = MaterialTheme.colorScheme.onSurface)
                            FeatureItem("🎨 深度个性化", "主题色、液态玻璃参数、文字与图标颜色模式。", color = MaterialTheme.colorScheme.onSurface)
                            FeatureItem("✨ 小而完整", "专注成绩管理与分析，不打扰，安静陪你记录每一次成长。", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 指示器
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(totalPages) { index ->
                    val selected = index == currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(if (selected) 20.dp else 8.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            if (currentPage > 0) {
                TextButton(
                    onClick = { currentPage -= 1 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text("上一页", color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Button(
                onClick = {
                    if (currentPage < totalPages - 1) {
                        currentPage += 1
                    } else {
                        onFinish()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    if (currentPage < totalPages - 1) "下一页" else "开始使用",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            if (currentPage < totalPages - 1) {
                TextButton(
                    onClick = {
                        onFinish()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("跳过介绍，直接进入", color = MaterialTheme.colorScheme.onSurface)
                }
            }
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
