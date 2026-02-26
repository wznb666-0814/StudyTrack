package com.repea.studytrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.ui.components.PieChartData
import com.repea.studytrack.ui.components.SimplePieChart
import com.repea.studytrack.ui.navigation.Screen
import com.repea.studytrack.viewmodel.AnalysisViewModel
import com.repea.studytrack.viewmodel.ExamViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    analysisViewModel: AnalysisViewModel = hiltViewModel(),
    examViewModel: ExamViewModel = hiltViewModel()
) {
    val recentExams by examViewModel.allRecords.collectAsState()
    val subjects by examViewModel.subjects.collectAsState()

    val pieData = remember(recentExams) {
        if (recentExams.isEmpty()) emptyList()
        else {
            val excellent = recentExams.count { it.exam.score / it.subject.fullScore >= 0.9 }
            val good = recentExams.count { val r = it.exam.score / it.subject.fullScore; r >= 0.8 && r < 0.9 }
            val pass = recentExams.count { val r = it.exam.score / it.subject.fullScore; r >= 0.6 && r < 0.8 }
            val fail = recentExams.count { it.exam.score / it.subject.fullScore < 0.6 }
            
            listOf(
                PieChartData("优", excellent.toFloat(), Color(0xFF4CAF50)), // Green
                PieChartData("良", good.toFloat(), Color(0xFF2196F3)), // Blue
                PieChartData("中", pass.toFloat(), Color(0xFFFFC107)), // Amber
                PieChartData("差", fail.toFloat(), Color(0xFFF44336))  // Red
            ).filter { it.value > 0 }
        }
    }

    // Removed LiquidBackground here because it's now at the root in MainActivity
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "StudyTrack",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Stats Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("近期考试", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("${recentExams.size}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("科目数量", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("${subjects.size}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Pie Chart Area
            if (pieData.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("成绩分布", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            SimplePieChart(
                                data = pieData,
                                modifier = Modifier.size(200.dp),
                                textColor = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("最近动态", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Use Column instead of LazyColumn inside a Column to avoid nested scrolling issues if any
                    // Or just limit items. Since we take 5, Column is fine and safer.
                    Column {
                        recentExams.take(5).forEach { exam ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(exam.subject.name, color = MaterialTheme.colorScheme.onSurface)
                                Text("${exam.exam.score}", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        if (recentExams.isEmpty()) {
                            Text("暂无考试记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { navController.navigate(Screen.AddExam.route) }) {
                Text("记成绩")
            }
            Button(onClick = { 
                navController.navigate(Screen.SubjectList.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }) {
                Text("管理科目")
            }
        }
    }
}
