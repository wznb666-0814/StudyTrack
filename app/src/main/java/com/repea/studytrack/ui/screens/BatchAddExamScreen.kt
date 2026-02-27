package com.repea.studytrack.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repea.studytrack.data.local.entity.ExamType
import com.repea.studytrack.data.local.entity.Subject
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.viewmodel.ExamViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class SubjectEntryUiState(
    val id: Long,
    val subject: Subject? = null,
    val score: String = "",
    val classRank: String = "",
    val gradeRank: String = "",
    val districtRank: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchAddExamScreen(
    navController: NavController,
    viewModel: ExamViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsState()

    var examName by remember { mutableStateOf("") }
    var selectedExamType by remember { mutableStateOf(ExamType.MONTHLY) }
    var examTypeExpanded by remember { mutableStateOf(false) }

    var examDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = examDate)

    var entries by remember { mutableStateOf(listOf(SubjectEntryUiState(id = 0L))) }
    var nextId by remember { mutableStateOf(1L) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        BasicAlertDialog(onDismissRequest = { showDatePicker = false }) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("选择考试日期", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier.scale(0.96f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { examDate = it }
                                showDatePicker = false
                            }
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("批量添加成绩", color = MaterialTheme.colorScheme.onSurface) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = examName,
                        onValueChange = { examName = it },
                        label = { Text("考试主题（如：九下第一次月考）") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(examDate)),
                        onValueChange = {},
                        label = { Text("考试日期") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )

                    ExposedDropdownMenuBox(
                        expanded = examTypeExpanded,
                        onExpandedChange = { examTypeExpanded = !examTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedExamType.label,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examTypeExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = examTypeExpanded,
                            onDismissRequest = { examTypeExpanded = false },
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            GlassCard(contentPadding = 8.dp) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    ExamType.values().forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type.label) },
                                            onClick = {
                                                selectedExamType = type
                                                examTypeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("科目成绩列表", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        IconButton(
                            onClick = {
                                entries = entries + SubjectEntryUiState(id = nextId++)
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加科目")
                        }
                    }

                    if (entries.isEmpty()) {
                        Text(
                            text = "请点击右上角 + 按钮添加科目。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    } else {
                        entries.forEach { entry ->
                            SubjectEntryCard(
                                entry = entry,
                                subjects = subjects,
                                onChange = { updated ->
                                    entries = entries.map { if (it.id == updated.id) updated else it }
                                },
                                onRemove = {
                                    entries = entries.filterNot { it.id == entry.id }
                                }
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    errorMessage = null

                    if (examName.isBlank()) {
                        errorMessage = "请先填写考试主题。"
                        return@Button
                    }

                    val validEntries = entries.mapNotNull { e ->
                        val subject = e.subject ?: return@mapNotNull null
                        val scoreVal = e.score.toDoubleOrNull() ?: return@mapNotNull null
                        if (scoreVal > subject.fullScore) return@mapNotNull null
                        Triple(subject, scoreVal, e)
                    }

                    if (validEntries.isEmpty()) {
                        errorMessage = "请至少为一个科目选择科目并填写有效分数。"
                        return@Button
                    }

                    validEntries.forEach { (subject, scoreVal, e) ->
                        viewModel.addRecord(
                            subjectId = subject.id,
                            examName = examName,
                            examDate = examDate,
                            score = scoreVal,
                            examType = selectedExamType,
                            classRank = e.classRank.toIntOrNull(),
                            gradeRank = e.gradeRank.toIntOrNull(),
                            districtRank = e.districtRank.toIntOrNull(),
                            reflection = null,
                            imageUri = null
                        )
                    }

                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存全部成绩")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectEntryCard(
    entry: SubjectEntryUiState,
    subjects: List<Subject>,
    onChange: (SubjectEntryUiState) -> Unit,
    onRemove: () -> Unit
) {
    var subjectExpanded by remember(entry.id) { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("科目", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                if (subjects.size > 1) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "移除此科目")
                    }
                }
            }

            if (subjects.isEmpty()) {
                Text(
                    text = "当前暂无科目，请先在「科目」页面添加科目。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                ) {
                    OutlinedTextField(
                        value = entry.subject?.name ?: "选择科目",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false },
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        GlassCard(contentPadding = 8.dp) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                subjects.forEach { subject ->
                                    DropdownMenuItem(
                                        text = { Text(subject.name) },
                                        onClick = {
                                            onChange(entry.copy(subject = subject))
                                            subjectExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = entry.score,
                    onValueChange = { onChange(entry.copy(score = it)) },
                    label = { Text("分数") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = entry.classRank,
                    onValueChange = { onChange(entry.copy(classRank = it)) },
                    label = { Text("班排") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = entry.gradeRank,
                    onValueChange = { onChange(entry.copy(gradeRank = it)) },
                    label = { Text("年排") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = entry.districtRank,
                    onValueChange = { onChange(entry.copy(districtRank = it)) },
                    label = { Text("区排") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}

