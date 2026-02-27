package com.repea.studytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repea.studytrack.data.local.entity.ExamWithSubject
import com.repea.studytrack.data.remote.DeepSeekClient
import com.repea.studytrack.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    repository: StudyRepository,
    private val deepSeekClient: DeepSeekClient
) : ViewModel() {
    val allRecords = repository.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 按科目（subjectId）缓存 AI 学习建议，避免重复请求
    private val _subjectAdvices = MutableStateFlow<Map<Int, String>>(emptyMap())
    val subjectAdvices: StateFlow<Map<Int, String>> = _subjectAdvices.asStateFlow()

    private val _loadingSubjects = MutableStateFlow<Set<Int>>(emptySet())
    val loadingSubjects: StateFlow<Set<Int>> = _loadingSubjects.asStateFlow()

    private val _errorSubjects = MutableStateFlow<Map<Int, String>>(emptyMap())
    val errorSubjects: StateFlow<Map<Int, String>> = _errorSubjects.asStateFlow()

    /**
     * 为单个科目生成 AI 学习建议。
     * subjectId 用来在内存中区分不同科目的建议。
     */
    fun requestAdviceForSubject(
        subjectId: Int,
        subjectName: String,
        fullScore: Double,
        records: List<ExamWithSubject>
    ) {
        if (records.isEmpty()) return

        // 已经在加载或已有结果时不重复请求
        if (_loadingSubjects.value.contains(subjectId) || _subjectAdvices.value.containsKey(subjectId)) {
            return
        }

        val sortedRecords = records.sortedBy { it.exam.examDate }
        val scores = sortedRecords.map { it.exam.score }
        val avgScore = scores.average()
        val maxScore = scores.maxOrNull() ?: 0.0
        val minScore = scores.minOrNull() ?: 0.0
        val latest = sortedRecords.last()

        val classRanks = sortedRecords.mapNotNull { it.exam.classRank }
        val gradeRanks = sortedRecords.mapNotNull { it.exam.gradeRank }
        val districtRanks = sortedRecords.mapNotNull { it.exam.districtRank }

        val prompt = buildString {
            appendLine("请根据下面这位学生在某一科目的历史成绩与排名趋势，给出 150~300 字左右的个性化学习建议。")
            appendLine("科目：$subjectName，满分：${fullScore.toInt()} 分。")
            appendLine(
                "共有 ${records.size} 次考试记录；最近一次考试分数：${
                    String.format(
                        "%.1f",
                        latest.exam.score
                    )
                } 分。"
            )
            appendLine(
                "平均分：${String.format("%.1f", avgScore)}，最高分：${
                    String.format(
                        "%.1f",
                        maxScore
                    )
                }，最低分：${String.format("%.1f", minScore)}。"
            )
            if (classRanks.isNotEmpty()) {
                appendLine("班级排名（按时间顺序）：${classRanks.joinToString(separator = ", ")}。")
            }
            if (gradeRanks.isNotEmpty()) {
                appendLine("年级排名（按时间顺序）：${gradeRanks.joinToString(separator = ", ")}。")
            }
            if (districtRanks.isNotEmpty()) {
                appendLine("区排名（按时间顺序）：${districtRanks.joinToString(separator = ", ")}。")
            }
            appendLine()
            appendLine("请你：")
            appendLine("1）先整体判断这门课当前的水平与变化趋势；")
            appendLine("2）分析可能存在的主要问题（如基础薄弱、粗心失分、题目难度、时间分配、复习节奏等）；")
            appendLine("3）分别从「短期提升策略」（1~2 次考试内）和「长期学习规划」（1 学期以上）两个角度，给出具体可执行的建议；")
            appendLine("4）可以适当举例说明具体做法，但不要复述题目内容。")
            appendLine("请用第二人称直接对学生说话，风格务实、清晰、条理分明。")
        }

        viewModelScope.launch {
            _loadingSubjects.update { it + subjectId }
            try {
                val advice = deepSeekClient.generateSubjectAdvice(prompt)
                _subjectAdvices.update { it + (subjectId to advice) }
                _errorSubjects.update { it - subjectId }
            } catch (e: Exception) {
                _errorSubjects.update { it + (subjectId to (e.message ?: "获取 AI 建议失败")) }
            } finally {
                _loadingSubjects.update { it - subjectId }
            }
        }
    }
}
