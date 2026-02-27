package com.repea.studytrack.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector = selectedIcon
) {
    object Home : Screen("home", "首页", Icons.Filled.Home, Icons.Outlined.Home)
    object SubjectList : Screen("subjects", "科目", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List)
    object ExamList : Screen("exams", "成绩", Icons.Filled.Star, Icons.Outlined.Star)
    object Analysis : Screen("analysis", "分析", Icons.Filled.DateRange, Icons.Outlined.DateRange)
    object Settings : Screen("settings", "设置", Icons.Filled.Settings, Icons.Outlined.Settings)
    
    // Non-bottom-bar screens
    object AddSubject : Screen("add_subject", "添加科目", Icons.Default.Add)
    object AddExam : Screen("add_exam", "添加成绩", Icons.Default.Add)
    object EditExam : Screen("edit_exam/{recordId}", "编辑成绩", Icons.Default.Edit)
    object BatchAddExam : Screen("batch_add_exam", "批量添加成绩", Icons.Default.Add)
    object Personalization : Screen("personalization", "个性化", Icons.Filled.Star, Icons.Outlined.Star)
    object Welcome : Screen("welcome", "欢迎", Icons.Filled.Home, Icons.Outlined.Home)
    object AiChat : Screen("ai_chat/{subjectId}", "AI 对话", Icons.Filled.Star, Icons.Outlined.Star)

    fun withRecordId(recordId: Int): String {
        return when (this) {
            EditExam -> "edit_exam/$recordId"
            else -> route
        }
    }
}
