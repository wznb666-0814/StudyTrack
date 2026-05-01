package com.repea.studytrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.ui.components.StudyTextField
import com.repea.studytrack.ui.navigation.Screen
import com.repea.studytrack.viewmodel.UserManagerViewModel

@Composable
fun WelcomeScreen(
    navController: NavController,
    onFinish: () -> Unit,
    userManagerViewModel: UserManagerViewModel = hiltViewModel()
) {
    var step by remember { mutableIntStateOf(0) }
    var userName by remember { mutableStateOf("") }
    var semesterName by remember { mutableStateOf("") }
    val featurePages = remember {
        listOf(
            OnboardingPage(
                title = "记录每一次成绩",
                description = "支持按科目录入分数、考试名称、日期与排名信息，后续统计会自动同步刷新。",
                points = listOf("单条录入更快捷", "新增与删除后首页、图表实时更新"),
                icon = Icons.Default.TableChart
            ),
            OnboardingPage(
                title = "分析趋势更清晰",
                description = "自动生成平均分、最高分与趋势折线图，帮助你看清波动与阶段变化。",
                points = listOf("按科目独立分析", "排名走势与成绩走势分开展示"),
                icon = Icons.Default.AutoGraph
            ),
            OnboardingPage(
                title = "Excel 导入导出",
                description = "支持将当前学期数据导出为 Excel，也支持按模板导入到当前学期。",
                points = listOf(
                    "导入列：学期、科目、考试名称、时间、分数、满分、分类、班排、年排、区排、反思",
                    "导出结构：每行一条成绩记录，包含所属学期与科目信息",
                    "示例：九年级上学期 / 数学 / 期中 / 2026-04-12 / 92 / 100"
                ),
                icon = Icons.Default.School
            )
        )
    }
    val pagerState = rememberPagerState(pageCount = { featurePages.size })

    fun finishOnboarding() {
        val finalName = userName.trim().ifBlank { "学习同学" }
        val finalSemesterName = semesterName.trim().ifBlank { "默认学期" }
        userManagerViewModel.initializePrimaryUser(finalName, finalSemesterName)
        onFinish()
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Welcome.route) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text(
                            text = "首次使用引导",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (step == 0) "先设置你的称呼" else "了解核心功能后即可开始使用",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
                        )
                    }
                }
            }

            if (step == 0) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = 20.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "步骤 1 / 2",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "请填写基础信息",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "用户名会用于首页问候，学期名会直接接入应用内的多学期管理。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StudyTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = "用户名",
                            supporting = {
                                Text("建议填写真实称呼或昵称，后续可在设置页修改")
                            }
                        )
                        StudyTextField(
                            value = semesterName,
                            onValueChange = { semesterName = it },
                            label = "当前学期名",
                            supporting = {
                                Text("例如：高一下学期、2026 春季学期，后续可在设置页继续管理")
                            }
                        )
                    }
                }
            } else {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = 18.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "步骤 2 / 2",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "核心功能介绍",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            OnboardingFeaturePage(page = featurePages[page])
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(featurePages.size) { index ->
                                val selected = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(width = if (selected) 22.dp else 8.dp, height = 8.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primary,
                onClick = {
                    if (step == 0) {
                        if (userName.trim().isNotBlank() && semesterName.trim().isNotBlank()) {
                            step = 1
                        }
                    } else {
                        finishOnboarding()
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (step == 0) "继续" else "完成引导",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        if (step == 0) {
                            finishOnboarding()
                        } else {
                            step = 0
                        }
                    }
                ) {
                    Text(if (step == 0) "跳过" else "上一步")
                }
                if (step == 1) {
                    TextButton(onClick = { finishOnboarding() }) {
                        Text("跳过介绍")
                    }
                }
            }
        }
    }
}

private data class OnboardingPage(
    val title: String,
    val description: String,
    val points: List<String>,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun OnboardingFeaturePage(page: OnboardingPage) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = page.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start
        )
        page.points.forEach { point ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .padding(top = 7.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = point,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
