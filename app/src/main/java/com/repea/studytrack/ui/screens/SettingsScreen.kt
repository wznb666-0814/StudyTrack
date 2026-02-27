package com.repea.studytrack.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.viewmodel.ImportUiResult
import com.repea.studytrack.viewmodel.SettingsViewModel

import android.content.Intent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.repea.studytrack.R

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import com.repea.studytrack.ui.navigation.Screen
import com.repea.studytrack.data.local.entity.UserProfile
import com.repea.studytrack.viewmodel.UserManagerViewModel
import com.repea.studytrack.viewmodel.UserPreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
    userManagerViewModel: UserManagerViewModel = hiltViewModel(),
    prefsViewModel: UserPreferencesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showImportHelp by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<UserProfile?>(null) }
    var userNameInput by remember { mutableStateOf("") }

    val userPrefs by userManagerViewModel.prefs.collectAsState()
    val gradePrefs by prefsViewModel.preferences.collectAsState()
    val users by userManagerViewModel.users.collectAsState()
    
    // Listen for export status
    LaunchedEffect(Unit) {
        viewModel.exportStatus.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    var importFailureGuide by remember { mutableStateOf<ImportUiResult.Failure?>(null) }
    LaunchedEffect(Unit) {
        viewModel.importResult.collect { result ->
            when (result) {
                is ImportUiResult.Success -> {
                    android.widget.Toast.makeText(context, "导入完成", android.widget.Toast.LENGTH_SHORT).show()
                }
                is ImportUiResult.Failure -> {
                    importFailureGuide = result
                }
            }
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.importData(it) }
        }
    )

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.exportData(it) }
        }
    )

    // Removed LiquidBackground (handled in MainActivity)
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("设置", color = MaterialTheme.colorScheme.onSurface) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
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
            // App Logo
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Screen.Personalization.route) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("个性化设置", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "壁纸、文字与图标颜色、液态玻璃参数",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("自定义成绩等级算法", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "为不同满分的科目设置 A/B/C 等级的起始分值（单位：分）。\nA 等级：分数 ≥ A 起始分；B 等级：分数 ≥ B 起始分；C 等级：分数 ≥ C 起始分；否则为 D 等级。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // 满分 100 分
                    Text("满分 100 分科目", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    var a100 by remember(gradePrefs.gradeA100) { mutableStateOf(gradePrefs.gradeA100.toString()) }
                    var b100 by remember(gradePrefs.gradeB100) { mutableStateOf(gradePrefs.gradeB100.toString()) }
                    var c100 by remember(gradePrefs.gradeC100) { mutableStateOf(gradePrefs.gradeC100.toString()) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = a100,
                            onValueChange = {
                                a100 = it
                                it.toFloatOrNull()?.let { v -> prefsViewModel.setGradeA100(v) }
                            },
                            label = { Text("A 起始分") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = b100,
                            onValueChange = {
                                b100 = it
                                it.toFloatOrNull()?.let { v -> prefsViewModel.setGradeB100(v) }
                            },
                            label = { Text("B 起始分") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = c100,
                            onValueChange = {
                                c100 = it
                                it.toFloatOrNull()?.let { v -> prefsViewModel.setGradeC100(v) }
                            },
                            label = { Text("C 起始分") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 满分 70 分
                    Text("满分 70 分科目", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    var a70 by remember(gradePrefs.gradeA70) { mutableStateOf(gradePrefs.gradeA70.toString()) }
                    var b70 by remember(gradePrefs.gradeB70) { mutableStateOf(gradePrefs.gradeB70.toString()) }
                    var c70 by remember(gradePrefs.gradeC70) { mutableStateOf(gradePrefs.gradeC70.toString()) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = a70,
                            onValueChange = {
                                a70 = it
                                it.toFloatOrNull()?.let { v -> prefsViewModel.setGradeA70(v) }
                            },
                            label = { Text("A 起始分") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = b70,
                            onValueChange = {
                                b70 = it
                                it.toFloatOrNull()?.let { v -> prefsViewModel.setGradeB70(v) }
                            },
                            label = { Text("B 起始分") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = c70,
                            onValueChange = {
                                c70 = it
                                it.toFloatOrNull()?.let { v -> prefsViewModel.setGradeC70(v) }
                            },
                            label = { Text("C 起始分") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 满分 60 分
                    Text("满分 60 分科目", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    var a60 by remember(gradePrefs.gradeA60) { mutableStateOf(gradePrefs.gradeA60.toString()) }
                    var b60 by remember(gradePrefs.gradeB60) { mutableStateOf(gradePrefs.gradeB60.toString()) }
                    var c60 by remember(gradePrefs.gradeC60) { mutableStateOf(gradePrefs.gradeC60.toString()) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = a60,
                            onValueChange = {
                                a60 = it
                                it.toFloatOrNull()?.let { v -> prefsViewModel.setGradeA60(v) }
                            },
                            label = { Text("A 起始分") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = b60,
                            onValueChange = {
                                b60 = it
                                it.toFloatOrNull()?.let { v -> prefsViewModel.setGradeB60(v) }
                            },
                            label = { Text("B 起始分") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = c60,
                            onValueChange = {
                                c60 = it
                                it.toFloatOrNull()?.let { v -> prefsViewModel.setGradeC60(v) }
                            },
                            label = { Text("C 起始分") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("数据管理", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    
                    Button(
                        onClick = { exportLauncher.launch("StudyTrack_Export_${System.currentTimeMillis()}.xlsx") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("导出成绩到 Excel")
                    }
                    
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("从 Excel 导入成绩")
                    }

                    TextButton(
                        onClick = { showImportHelp = !showImportHelp },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(if (showImportHelp) "收起导入格式说明" else "查看导入格式说明")
                    }

                    if (showImportHelp) {
                        Text(
                            text = "Excel 首行请按以下顺序作为表头（必须是中文字段名）：\n" +
                                "1. 科目\n" +
                                "2. 考试名称\n" +
                                "3. 时间（格式：yyyy-MM-dd）\n" +
                                "4. 分数\n" +
                                "5. 满分（用于创建科目时设置满分分值）\n" +
                                "6. 分类（如：期中、期末，可为空）\n" +
                                "7. 班排\n" +
                                "8. 年排\n" +
                                "9. 区排\n" +
                                "10. 反思\n\n" +
                                "导入时会按「科目」名称匹配已有科目；如不存在则自动新建，并使用「满分」列作为该科目的满分分值。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("多用户模式", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "为不同使用者分别记录成绩与科目数据",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = userPrefs.multiUserEnabled,
                            onCheckedChange = { enabled ->
                                userManagerViewModel.setMultiUserEnabled(enabled)
                            }
                        )
                    }

                    if (userPrefs.multiUserEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "用户列表",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        users.forEach { user ->
                            val isCurrent = user.id == userPrefs.currentUserId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { userManagerViewModel.setCurrentUser(user.id) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isCurrent) "${user.name}（当前）" else user.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(onClick = {
                                    editingUser = user
                                    userNameInput = user.name
                                    showUserDialog = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "重命名用户",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                                val canDelete = users.size > 1
                                IconButton(
                                    onClick = { if (canDelete) userManagerViewModel.deleteUser(user) },
                                    enabled = canDelete
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除用户",
                                        tint = if (canDelete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.3f
                                        )
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                editingUser = null
                                userNameInput = ""
                                showUserDialog = true
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("添加用户")
                        }
                    }
                }
            }

            if (importFailureGuide != null) {
                val failure = importFailureGuide!!
                AlertDialog(
                    onDismissRequest = { importFailureGuide = null },
                    title = { Text("导入失败") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(failure.message, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "正确格式说明：",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                failure.formatGuide,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { importFailureGuide = null }) {
                            Text("知道了")
                        }
                    }
                )
            }
            
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("关于", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("学迹 (StudyTrack)", color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "一款专注学生个人学业成长的本地成绩管理与分析应用，支持多用户、多科目、多考试场景下的成绩记录、趋势分析与 AI 学习助手。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "关于作者",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Author Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val intent =
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.coolapk.com/u/24128753"))
                                context.startActivity(intent)
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "酷安@在摆烂中沉沦",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "点击访问主页",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

                    // 个人主页
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://repea.top/"))
                                context.startActivity(intent)
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "个人主页",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "点击访问主页",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // 鸣谢列表
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "鸣谢",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/Kyant0/AndroidLiquidGlass")
                                )
                                context.startActivity(intent)
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Android Liquid Glass",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Compose Multiplatform Liquid Glass effect",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (showUserDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showUserDialog = false
                        editingUser = null
                        userNameInput = ""
                    },
                    title = {
                        Text(
                            if (editingUser == null) "添加用户" else "重命名用户",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    text = {
                        OutlinedTextField(
                            value = userNameInput,
                            onValueChange = { userNameInput = it },
                            label = { Text("用户名称") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val name = userNameInput.trim()
                                if (name.isNotEmpty()) {
                                    val user = editingUser
                                    if (user == null) {
                                        userManagerViewModel.addUser(name)
                                    } else {
                                        userManagerViewModel.renameUser(user, name)
                                    }
                                    showUserDialog = false
                                    editingUser = null
                                    userNameInput = ""
                                }
                            }
                        ) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showUserDialog = false
                                editingUser = null
                                userNameInput = ""
                            }
                        ) {
                            Text("取消")
                        }
                    }
                )
            }
        }
    }
}
