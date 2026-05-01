package com.repea.studytrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repea.studytrack.data.local.entity.ExamWithSubject
import com.repea.studytrack.data.local.entity.Subject
import com.repea.studytrack.repository.UserPreferencesState
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.ui.components.GlassDropdownMenu
import com.repea.studytrack.ui.components.SimpleLineChart
import com.repea.studytrack.ui.components.StudyCapsuleButton
import com.repea.studytrack.ui.components.StudyHeroCard
import com.repea.studytrack.ui.components.StudyMetricCard
import com.repea.studytrack.ui.components.StudySectionHeader
import com.repea.studytrack.utils.GradeCalculator
import com.repea.studytrack.viewmodel.AnalysisViewModel
import com.repea.studytrack.viewmodel.ExamViewModel
import com.repea.studytrack.viewmodel.UserManagerViewModel
import com.repea.studytrack.viewmodel.UserPreferencesViewModel

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = hiltViewModel(),
    examViewModel: ExamViewModel = hiltViewModel(),
    userManagerViewModel: UserManagerViewModel = hiltViewModel(),
    prefsViewModel: UserPreferencesViewModel = hiltViewModel()
) {
    val allRecords by viewModel.allRecords.collectAsState()
    val subjects by examViewModel.subjects.collectAsState()
    val userPrefs by userManagerViewModel.prefs.collectAsState()
    val semesters by userManagerViewModel.semesters.collectAsState()
    val gradePrefs by prefsViewModel.preferences.collectAsState()
    val currentSemester = remember(semesters, userPrefs.currentSemesterId) {
        semesters.firstOrNull { it.id == userPrefs.currentSemesterId } ?: semesters.firstOrNull()
    }

    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val filteredRecords = remember(allRecords, selectedSubject) {
        if (selectedSubject == null) {
            allRecords
        } else {
            allRecords.filter { it.subject.id == selectedSubject!!.id }
        }
    }
    val recordsBySubject = filteredRecords.groupBy { it.subject }
    val averageScore = remember(filteredRecords) {
        if (filteredRecords.isEmpty()) 0.0 else filteredRecords.map { it.exam.score }.average()
    }
    val bestScore = filteredRecords.maxOfOrNull { it.exam.score } ?: 0.0

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "成绩分析",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "查看各科成绩、评级与排名走势",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "当前学期：${currentSemester?.name ?: "默认学期"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            item {
                StudyHeroCard(
                    title = "分析概览",
                    value = if (filteredRecords.isEmpty()) "--" else String.format("%.1f", averageScore),
                    badge = "${filteredRecords.size} 次考试",
                    subtitle = buildString {
                        append(currentSemester?.name ?: "默认学期")
                        append(" · ")
                        append(selectedSubject?.name ?: "全部科目")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StudyMetricCard(
                        title = "最高分",
                        value = if (filteredRecords.isEmpty()) "--" else formatAnalysisScore(bestScore),
                        note = "当前筛选",
                        modifier = Modifier.weight(1f)
                    )
                    StudyMetricCard(
                        title = "科目数",
                        value = recordsBySubject.size.toString(),
                        note = "参与分析",
                        modifier = Modifier.weight(1f),
                        accent = Color(0xFF24B46B)
                    )
                }
            }
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = 14.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StudySectionHeader(title = "分析筛选")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StudyCapsuleButton(
                                selected = selectedSubject == null,
                                text = "全部",
                                onClick = { selectedSubject = null }
                            )
                            Box {
                                StudyCapsuleButton(
                                    selected = selectedSubject != null,
                                    text = selectedSubject?.name ?: "选择科目",
                                    onClick = { showFilterMenu = true }
                                )
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
                        }
                    }
                }
            }
            item {
                StudySectionHeader(title = "分析结果")
            }
            if (recordsBySubject.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "暂无数据可分析",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "先录入一些成绩后，这里会展示趋势、评级与排名变化。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(recordsBySubject.entries.toList()) { (subject, records) ->
                    SubjectAnalysisCard(
                        subject = subject,
                        records = records,
                        gradePrefs = gradePrefs
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectAnalysisCard(
    subject: Subject,
    records: List<ExamWithSubject>,
    gradePrefs: UserPreferencesState
) {
    if (records.isEmpty()) return

    val sortedRecords = records.sortedBy { it.exam.examDate }
    val scores = sortedRecords.map { it.exam.score }
    val avgScore = scores.average()
    val maxScore = scores.maxOrNull() ?: 0.0
    val minScore = scores.minOrNull() ?: 0.0
    val latestRecord = sortedRecords.last()
    val latestGrade = GradeCalculator.calculateGradeCustom(
        score = latestRecord.exam.score,
        fullScore = subject.fullScore,
        prefs = gradePrefs
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.name.take(1),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "满分 ${formatAnalysisScore(subject.fullScore)} · 最新评级 $latestGrade",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StudyMetricCard(
                    title = "平均分",
                    value = String.format("%.1f", avgScore),
                    note = "共 ${records.size} 次",
                    modifier = Modifier.weight(1f)
                )
                StudyMetricCard(
                    title = "最高分",
                    value = String.format("%.1f", maxScore),
                    note = "最低 ${String.format("%.1f", minScore)}",
                    modifier = Modifier.weight(1f),
                    accent = Color(0xFF24B46B)
                )
            }
            StudySectionHeader(title = "成绩趋势")
            SimpleLineChart(
                dataPoints = scores,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                lineColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onSurface
            )
            val classRanks = sortedRecords.mapNotNull { it.exam.classRank?.toDouble() }
            if (classRanks.size > 1) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                Text(
                    text = "班级排名趋势",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SimpleLineChart(
                    dataPoints = classRanks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    lineColor = Color(0xFF24B46B),
                    textColor = MaterialTheme.colorScheme.onSurface,
                    isRanking = true
                )
            }
            val gradeRanks = sortedRecords.mapNotNull { it.exam.gradeRank?.toDouble() }
            if (gradeRanks.size > 1) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                Text(
                    text = "年级排名趋势",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SimpleLineChart(
                    dataPoints = gradeRanks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    lineColor = Color(0xFFFFA63D),
                    textColor = MaterialTheme.colorScheme.onSurface,
                    isRanking = true
                )
            }
            val districtRanks = sortedRecords.mapNotNull { it.exam.districtRank?.toDouble() }
            if (districtRanks.size > 1) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                Text(
                    text = "全区排名趋势",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SimpleLineChart(
                    dataPoints = districtRanks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    lineColor = Color(0xFF5C7CFA),
                    textColor = MaterialTheme.colorScheme.onSurface,
                    isRanking = true
                )
            }
        }
    }
}

private fun formatAnalysisScore(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format("%.1f", value)
    }
}
