package com.repea.studytrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.viewmodel.SubjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    navController: NavController,
    viewModel: SubjectViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }
    var newSubjectScore by remember { mutableStateOf("100") }

    if (showDialog) {
        BasicAlertDialog(onDismissRequest = { showDialog = false }) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("添加科目", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newSubjectName,
                            onValueChange = { newSubjectName = it },
                            label = { Text("科目名称") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newSubjectScore,
                            onValueChange = { newSubjectScore = it },
                            label = { Text("满分") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                if (newSubjectName.isNotBlank() && newSubjectScore.toDoubleOrNull() != null) {
                                    viewModel.addSubject(newSubjectName, newSubjectScore.toDouble())
                                    showDialog = false
                                    newSubjectName = ""
                                    newSubjectScore = "100"
                                }
                            }
                        ) {
                            Text("添加")
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
                title = { Text("科目管理", color = MaterialTheme.colorScheme.onSurface) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Subject")
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
            items(subjects) { subject ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(subject.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("满分: ${subject.fullScore}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        IconButton(onClick = { viewModel.deleteSubject(subject) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}
