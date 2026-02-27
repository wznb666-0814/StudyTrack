package com.repea.studytrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repea.studytrack.data.local.entity.ExamWithSubject
import com.repea.studytrack.data.local.entity.Subject
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.ui.components.GlassDropdownMenu
import com.repea.studytrack.ui.components.MarkdownText
import com.repea.studytrack.ui.components.SimpleLineChart
import com.repea.studytrack.utils.GradeCalculator
import com.repea.studytrack.viewmodel.AnalysisViewModel
import com.repea.studytrack.viewmodel.UserPreferencesViewModel
import com.repea.studytrack.viewmodel.ExamViewModel // Assuming ExamViewModel has subjects flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    navController: NavController,
    viewModel: AnalysisViewModel = hiltViewModel(),
    examViewModel: ExamViewModel = hiltViewModel(), // Inject ExamViewModel to get subjects
    prefsViewModel: UserPreferencesViewModel = hiltViewModel()
) {
    val allRecords by viewModel.allRecords.collectAsState()
    val subjects by examViewModel.subjects.collectAsState()
    val gradePrefs by prefsViewModel.preferences.collectAsState()

    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    // Filter records by subject
    val filteredRecords = remember(allRecords, selectedSubject) {
        if (selectedSubject == null) {
            allRecords
        } else {
            allRecords.filter { it.subject.id == selectedSubject!!.id }
        }
    }
    
    // Group records by subject
    val recordsBySubject = filteredRecords.groupBy { it.subject }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("成绩分析", color = MaterialTheme.colorScheme.onSurface) },
                actions = {
                    Box {
                        TextButton(onClick = { showFilterMenu = true }) {
                            Text(selectedSubject?.name ?: "全部科目", color = MaterialTheme.colorScheme.onSurface)
                        }
                        GlassDropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("全部科目") },
                                onClick = {
                                    selectedSubject = null
                                    showFilterMenu = false
                                }
                            )
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = {
                                        selectedSubject = subject
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            val isWideScreen = maxWidth > 600.dp

            if (isWideScreen) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 400.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    if (recordsBySubject.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("暂无数据可分析", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    } else {
                        items(recordsBySubject.entries.toList()) { (subject, records) ->
                            SubjectAnalysisCard(
                                navController = navController,
                                subject = subject,
                                records = records,
                                viewModel = viewModel,
                                gradePrefs = gradePrefs
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    if (recordsBySubject.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("暂无数据可分析", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    } else {
                        items(recordsBySubject.entries.toList()) { (subject, records) ->
                            SubjectAnalysisCard(
                                navController = navController,
                                subject = subject,
                                records = records,
                                viewModel = viewModel,
                                gradePrefs = gradePrefs
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectAnalysisCard(
    navController: NavController,
    subject: Subject,
    records: List<ExamWithSubject>,
    viewModel: AnalysisViewModel,
    gradePrefs: com.repea.studytrack.repository.UserPreferencesState
) {
    if (records.isEmpty()) return

    val sortedRecords = records.sortedBy { it.exam.examDate }
    val scores = sortedRecords.map { it.exam.score }
    
    val avgScore = scores.average()
    val maxScore = scores.maxOrNull() ?: 0.0
    val minScore = scores.minOrNull() ?: 0.0
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(subject.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "满分: ${subject.fullScore.toInt()}",
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("平均分", String.format("%.1f", avgScore))
                StatItem("最高分", String.format("%.1f", maxScore))
                StatItem("最低分", String.format("%.1f", minScore))
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Score Chart
            Text("成绩趋势", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            SimpleLineChart(
                dataPoints = scores,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                lineColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onSurface
            )
            
            // Latest Grade
            val latestRecord = sortedRecords.last()
        val grade = GradeCalculator.calculateGradeCustom(
            score = latestRecord.exam.score,
            fullScore = subject.fullScore,
            prefs = gradePrefs
        )
            Text(
                "最新评级: $grade", 
                style = MaterialTheme.typography.bodyLarge, 
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.End)
            )

            // Rankings Charts
            val classRanks = sortedRecords.mapNotNull { it.exam.classRank?.toDouble() }
            if (classRanks.isNotEmpty() && classRanks.size > 1) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("班级排名趋势", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                SimpleLineChart(
                    dataPoints = classRanks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    lineColor = Color(0xFF4CAF50), // Green for rank
                    textColor = MaterialTheme.colorScheme.onSurface,
                    isRanking = true
                )
            }

            val gradeRanks = sortedRecords.mapNotNull { it.exam.gradeRank?.toDouble() }
            if (gradeRanks.isNotEmpty() && gradeRanks.size > 1) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("年级排名趋势", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                SimpleLineChart(
                    dataPoints = gradeRanks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    lineColor = Color(0xFFFF9800), // Orange for rank
                    textColor = MaterialTheme.colorScheme.onSurface,
                    isRanking = true
                )
            }
            
            val districtRanks = sortedRecords.mapNotNull { it.exam.districtRank?.toDouble() }
            if (districtRanks.isNotEmpty() && districtRanks.size > 1) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("全区排名趋势", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                SimpleLineChart(
                    dataPoints = districtRanks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    lineColor = Color(0xFF2196F3), // Blue for rank
                    textColor = MaterialTheme.colorScheme.onSurface,
                    isRanking = true
                )
            }

            // AI 学习建议（结合该科目的分数与排名趋势）
            val subjectAdvices by viewModel.subjectAdvices.collectAsState()
            val loadingSubjects by viewModel.loadingSubjects.collectAsState()
            val errorSubjects by viewModel.errorSubjects.collectAsState()

            val advice = subjectAdvices[subject.id]
            val isLoading = loadingSubjects.contains(subject.id)
            val error = errorSubjects[subject.id]

            LaunchedEffect(subject.id, records.size) {
                viewModel.requestAdviceForSubject(
                    subjectId = subject.id,
                    subjectName = subject.name,
                    fullScore = subject.fullScore,
                    records = records
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "AI 学习建议",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (isLoading && advice == null) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            when {
                advice != null -> {
                    MarkdownText(
                        text = advice,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }

                !isLoading && error != null -> {
                    Text(
                        text = "获取 AI 建议失败：$error",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                !isLoading -> {
                    Text(
                        text = "正在为你分析这门课的成绩与排名，请稍候片刻…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        navController.navigate(
                            com.repea.studytrack.ui.navigation.Screen.AiChat.route.replace(
                                "{subjectId}",
                                subject.id.toString()
                            )
                        )
                    }
                ) {
                    Text("对话")
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}
