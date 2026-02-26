package com.repea.studytrack.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExamScreen(
    navController: NavController,
    viewModel: ExamViewModel = hiltViewModel(),
    recordId: Int? = null
) {
    val subjects by viewModel.subjects.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var expanded by remember { mutableStateOf(false) }
    
    var examName by remember { mutableStateOf("") }
    var score by remember { mutableStateOf("") }
    var scoreError by remember { mutableStateOf<String?>(null) }
    var classRank by remember { mutableStateOf("") }
    var gradeRank by remember { mutableStateOf("") }
    var districtRank by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }
    
    var selectedExamType by remember { mutableStateOf(ExamType.MONTHLY) }
    var typeExpanded by remember { mutableStateOf(false) }

    var examDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = examDate)
    val editingRecord = remember(recordId, allRecords) {
        recordId?.let { id -> allRecords.firstOrNull { it.exam.id == id } }
    }
    var initialized by remember(recordId) { mutableStateOf(false) }

    LaunchedEffect(editingRecord) {
        if (!initialized && editingRecord != null) {
            selectedSubject = editingRecord.subject
            examName = editingRecord.exam.examName
            score = editingRecord.exam.score.toString()
            classRank = editingRecord.exam.classRank?.toString() ?: ""
            gradeRank = editingRecord.exam.gradeRank?.toString() ?: ""
            districtRank = editingRecord.exam.districtRank?.toString() ?: ""
            reflection = editingRecord.exam.reflection ?: ""
            selectedExamType = ExamType.fromLabel(editingRecord.exam.examType)
            examDate = editingRecord.exam.examDate
            datePickerState.selectedDateMillis = editingRecord.exam.examDate
            initialized = true
        }
    }

    if (showDatePicker) {
        BasicAlertDialog(onDismissRequest = { showDatePicker = false }) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("选择日期", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
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

    // Removed LiquidBackground (handled in MainActivity)
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(if (recordId == null) "添加成绩" else "编辑成绩") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
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
                    // Subject Selection
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject?.name ?: "选择科目",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
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
                                                selectedSubject = subject
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Exam Date
                    OutlinedTextField(
                        value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(examDate)),
                        onValueChange = {},
                        label = { Text("考试日期") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
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

                    // Exam Name
                    OutlinedTextField(
                        value = examName,
                        onValueChange = { examName = it },
                        label = { Text("考试名称 (如: 第一次月考)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Exam Type
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedExamType.label,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false },
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
                                                typeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Score
                    OutlinedTextField(
                        value = score,
                        onValueChange = { 
                            score = it
                            scoreError = null
                            val value = it.toDoubleOrNull()
                            if (value != null && selectedSubject != null) {
                                    if (value > selectedSubject!!.fullScore) {
                                        scoreError = "分数不能超过满分 (${selectedSubject!!.fullScore})"
                                    }
                            }
                        },
                        label = { Text("分数") },
                        isError = scoreError != null,
                        supportingText = { if (scoreError != null) Text(scoreError!!) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Ranks
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = classRank,
                            onValueChange = { classRank = it },
                            label = { Text("班排") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = gradeRank,
                            onValueChange = { gradeRank = it },
                            label = { Text("年排") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                         OutlinedTextField(
                            value = districtRank,
                            onValueChange = { districtRank = it },
                            label = { Text("区排") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Reflection
                    OutlinedTextField(
                        value = reflection,
                        onValueChange = { reflection = it },
                        label = { Text("考试反思") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Button(
                        onClick = {
                            val scoreVal = score.toDoubleOrNull()
                            if (selectedSubject != null && scoreVal != null) {
                                if (scoreVal > selectedSubject!!.fullScore) {
                                    scoreError = "分数不能超过满分"
                                    return@Button
                                }
                                if (recordId != null && editingRecord != null) {
                                    viewModel.updateRecord(
                                        editingRecord.exam.copy(
                                            subjectId = selectedSubject!!.id,
                                            examName = examName,
                                            examDate = examDate,
                                            score = scoreVal,
                                            examType = selectedExamType.label,
                                            classRank = classRank.toIntOrNull(),
                                            gradeRank = gradeRank.toIntOrNull(),
                                            districtRank = districtRank.toIntOrNull(),
                                            reflection = reflection
                                        )
                                    )
                                } else {
                                    viewModel.addRecord(
                                        subjectId = selectedSubject!!.id,
                                        examName = examName,
                                        examDate = examDate,
                                        score = scoreVal,
                                        examType = selectedExamType,
                                        classRank = classRank.toIntOrNull(),
                                        gradeRank = gradeRank.toIntOrNull(),
                                        districtRank = districtRank.toIntOrNull(),
                                        reflection = reflection,
                                        imageUri = null
                                    )
                                }
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (recordId == null) "保存记录" else "保存修改")
                    }
                }
            }
        }
    }
}
