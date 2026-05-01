package com.repea.studytrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repea.studytrack.data.local.entity.Subject
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.ui.components.GlassDropdownMenu
import com.repea.studytrack.ui.components.StudyCapsuleButton
import com.repea.studytrack.ui.components.StudyHeroCard
import com.repea.studytrack.ui.components.StudyMetricCard
import com.repea.studytrack.ui.components.StudySectionHeader
import com.repea.studytrack.ui.components.StudyTextField
import com.repea.studytrack.ui.navigation.Screen
import com.repea.studytrack.viewmodel.ExamViewModel
import com.repea.studytrack.viewmodel.UserManagerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamListScreen(
    navController: NavController,
    viewModel: ExamViewModel = hiltViewModel(),
    userManagerViewModel: UserManagerViewModel = hiltViewModel()
) {
    val allExams by viewModel.allRecords.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val userPrefs by userManagerViewModel.prefs.collectAsState()
    val semesters by userManagerViewModel.semesters.collectAsState()
    val currentSemester = remember(semesters, userPrefs.currentSemesterId) {
        semesters.firstOrNull { it.id == userPrefs.currentSemesterId } ?: semesters.firstOrNull()
    }

    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showDeleteDialogForId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val displayedExams = remember(allExams, selectedSubject, searchQuery) {
        val subjectFiltered = if (selectedSubject == null) {
            allExams
        } else {
            allExams.filter { it.subject.id == selectedSubject!!.id }
        }
        if (searchQuery.isBlank()) {
            subjectFiltered
        } else {
            subjectFiltered.filter { it.exam.examName.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }
    val avgScore = remember(displayedExams) {
        if (displayedExams.isEmpty()) 0.0 else displayedExams.map { it.exam.score }.average()
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddExam.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加成绩")
                    Text("添加成绩")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "成绩记录",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "集中管理你的每一次考试成绩",
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
                    title = "当前筛选成绩",
                    value = if (displayedExams.isEmpty()) "--" else String.format("%.1f", avgScore),
                    badge = "${displayedExams.size} 条记录",
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
                        title = "科目",
                        value = selectedSubject?.name ?: "全部",
                        note = "当前筛选",
                        modifier = Modifier.weight(1f)
                    )
                    StudyMetricCard(
                        title = "最高分",
                        value = displayedExams.maxOfOrNull { formatExamScore(it.exam.score) } ?: "--",
                        note = "筛选结果",
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
                        StudyTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "搜索考试名称",
                            trailing = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        StudySectionHeader(title = "筛选科目")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                            TextButton(onClick = { navController.navigate(Screen.BatchAddExam.route) }) {
                                Text("批量添加")
                            }
                        }
                    }
                }
            }
            item {
                StudySectionHeader(title = "全部记录")
            }
            if (displayedExams.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "没有匹配的成绩记录",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "可以尝试切换筛选，或者添加新的考试成绩。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(displayedExams.sortedByDescending { it.exam.examDate }) { examWithSubject ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = 14.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = examWithSubject.subject.name.take(1),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = examWithSubject.subject.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = examWithSubject.exam.examName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatExamScore(examWithSubject.exam.score),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = formatDate(examWithSubject.exam.examDate),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.88f))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = buildRankText(
                                        examWithSubject.exam.classRank,
                                        examWithSubject.exam.gradeRank,
                                        examWithSubject.exam.districtRank
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        navController.navigate(Screen.EditExam.withRecordId(examWithSubject.exam.id))
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "编辑成绩")
                                }
                                IconButton(
                                    onClick = { showDeleteDialogForId = examWithSubject.exam.id }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除成绩",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showDeleteDialogForId?.let { recordId ->
        val record = allExams.firstOrNull { it.exam.id == recordId }?.exam
        if (record != null) {
            BasicAlertDialog(onDismissRequest = { showDeleteDialogForId = null }) {
                GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "删除成绩",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "确认删除这条成绩记录吗？删除后无法恢复。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showDeleteDialogForId = null }) {
                                Text("取消")
                            }
                            TextButton(
                                onClick = {
                                    viewModel.deleteRecord(record)
                                    showDeleteDialogForId = null
                                }
                            ) {
                                Text("删除")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatExamScore(score: Double): String {
    return if (score % 1.0 == 0.0) {
        score.toInt().toString()
    } else {
        String.format("%.1f", score)
    }
}

private fun formatDate(timeMillis: Long): String {
    return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(timeMillis))
}

private fun buildRankText(classRank: Int?, gradeRank: Int?, districtRank: Int?): String {
    val parts = buildList {
        if (classRank != null) add("班排 $classRank")
        if (gradeRank != null) add("年排 $gradeRank")
        if (districtRank != null) add("区排 $districtRank")
    }
    return if (parts.isEmpty()) "暂无排名信息" else parts.joinToString(" · ")
}
