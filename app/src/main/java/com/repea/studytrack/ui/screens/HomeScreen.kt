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
import com.repea.studytrack.utils.GradeCalculator
import com.repea.studytrack.viewmodel.AnalysisViewModel
import com.repea.studytrack.viewmodel.ExamViewModel
import com.repea.studytrack.viewmodel.UserManagerViewModel
import com.repea.studytrack.viewmodel.UserPreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    analysisViewModel: AnalysisViewModel = hiltViewModel(),
    examViewModel: ExamViewModel = hiltViewModel(),
    userManagerViewModel: UserManagerViewModel = hiltViewModel(),
    prefsViewModel: UserPreferencesViewModel = hiltViewModel()
) {
    val recentExams by examViewModel.allRecords.collectAsState()
    val subjects by examViewModel.subjects.collectAsState()

    val userPrefs by userManagerViewModel.prefs.collectAsState()
    val users by userManagerViewModel.users.collectAsState()
    val gradePrefs by prefsViewModel.preferences.collectAsState()

    val colorScheme = MaterialTheme.colorScheme

    val pieData = remember(recentExams, colorScheme, gradePrefs) {
        if (recentExams.isEmpty()) emptyList()
        else {
            var countA = 0
            var countB = 0
            var countC = 0
            var countD = 0

            recentExams.forEach { e ->
                val grade = GradeCalculator.calculateGradeCustom(
                    score = e.exam.score,
                    fullScore = e.subject.fullScore,
                    prefs = gradePrefs
                )
                when (grade) {
                    "A" -> countA++
                    "B" -> countB++
                    "C" -> countC++
                    else -> countD++
                }
            }
            
            listOf(
                PieChartData("A", countA.toFloat(), colorScheme.tertiary.copy(alpha = 0.95f)),
                PieChartData("B", countB.toFloat(), colorScheme.primary.copy(alpha = 0.9f)),
                PieChartData("C", countC.toFloat(), colorScheme.primary.copy(alpha = 0.65f)),
                PieChartData("D", countD.toFloat(), colorScheme.error.copy(alpha = 0.8f))
            ).filter { it.value > 0 }
        }
    }

    // Removed LiquidBackground here because it's now at the root in MainActivity
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (userPrefs.multiUserEnabled && users.isNotEmpty()) {
                var menuExpanded by remember { mutableStateOf(false) }
                val currentUser = users.firstOrNull { it.id == userPrefs.currentUserId } ?: users.first()

                ExposedDropdownMenuBox(
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = currentUser.name,
                        onValueChange = {},
                        label = { Text("当前用户") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        users.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user.name) },
                                onClick = {
                                    userManagerViewModel.setCurrentUser(user.id)
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

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
            
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Quick Actions：适当下移，上下边距均衡
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 0.dp),
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
