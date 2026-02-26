package com.repea.studytrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repea.studytrack.data.local.entity.Subject
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.ui.components.GlassDropdownMenu
import com.repea.studytrack.ui.navigation.Screen
import com.repea.studytrack.viewmodel.ExamViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamListScreen(
    navController: NavController,
    viewModel: ExamViewModel = hiltViewModel()
) {
    val allExams by viewModel.allRecords.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showDeleteDialogForId by remember { mutableStateOf<Int?>(null) }
    
    val displayedExams = remember(allExams, selectedSubject) {
        if (selectedSubject == null) {
            allExams
        } else {
            allExams.filter { it.subject.id == selectedSubject!!.id }
        }
    }

    // Removed LiquidBackground (handled in MainActivity)
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("成绩记录", color = MaterialTheme.colorScheme.onSurface) },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddExam.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Exam")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayedExams) { examWithSubject ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = examWithSubject.subject.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = examWithSubject.exam.examName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(examWithSubject.exam.examDate)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${examWithSubject.exam.score}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row {
                                    IconButton(
                                        onClick = {
                                            navController.navigate(Screen.EditExam.withRecordId(examWithSubject.exam.id))
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                                    }
                                    IconButton(
                                        onClick = { showDeleteDialogForId = examWithSubject.exam.id }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除")
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        if (examWithSubject.exam.classRank != null) {
                            Text(
                                text = "班排: ${examWithSubject.exam.classRank} | 年排: ${examWithSubject.exam.gradeRank ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
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
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("删除成绩", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("确认删除该次成绩记录吗？此操作无法撤销。", color = MaterialTheme.colorScheme.onSurface)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showDeleteDialogForId = null }) {
                                Text("取消")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
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
