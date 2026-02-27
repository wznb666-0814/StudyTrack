package com.repea.studytrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repea.studytrack.data.local.entity.Subject
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.viewmodel.SubjectViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    navController: NavController,
    viewModel: SubjectViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }
    var newSubjectName by remember { mutableStateOf("") }
    var newSubjectScore by remember { mutableStateOf("100") }
    val listState = rememberLazyListState()
    var fabExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        var prevIndex = listState.firstVisibleItemIndex
        var prevOffset = listState.firstVisibleItemScrollOffset
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collectLatest { (index, offset) ->
                val isScrollingDown = index > prevIndex || (index == prevIndex && offset > prevOffset)
                if (isScrollingDown) {
                    fabExpanded = false
                } else if (index < prevIndex || offset < prevOffset) {
                    fabExpanded = true
                }
                prevIndex = index
                prevOffset = offset
            }
    }

    if (showDialog) {
        BasicAlertDialog(onDismissRequest = { showDialog = false }) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (editingSubject == null) "添加科目" else "编辑科目",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
                            val score = newSubjectScore.toDoubleOrNull()
                            if (newSubjectName.isNotBlank() && score != null) {
                                val subject = editingSubject
                                if (subject == null) {
                                    viewModel.addSubject(newSubjectName, score)
                                } else {
                                    viewModel.updateSubject(subject, newSubjectName, score)
                                }
                                showDialog = false
                                editingSubject = null
                                newSubjectName = ""
                                newSubjectScore = "100"
                            }
                        }
                        ) {
                            Text(if (editingSubject == null) "添加" else "保存")
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
            SubjectShrinkableFab(expanded = fabExpanded) {
                editingSubject = null
                newSubjectName = ""
                newSubjectScore = "100"
                showDialog = true
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(subjects) { subject ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                subject.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                "满分: ${subject.fullScore}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    editingSubject = subject
                                    newSubjectName = subject.name
                                    newSubjectScore = subject.fullScore.toString()
                                    showDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface)
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
}

@Composable
private fun SubjectShrinkableFab(
    expanded: Boolean,
    onClick: () -> Unit
) {
    val targetSize = if (expanded) 56.dp else 44.dp
    val targetIconScale = if (expanded) 1f else 0.9f

    val size by animateDpAsState(
        targetValue = targetSize,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "subjectFabSize"
    )
    val iconScale by animateFloatAsState(
        targetValue = targetIconScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "subjectFabIconScale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(size)
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Add Subject",
            modifier = Modifier.graphicsLayer(
                scaleX = iconScale,
                scaleY = iconScale
            )
        )
    }
}
