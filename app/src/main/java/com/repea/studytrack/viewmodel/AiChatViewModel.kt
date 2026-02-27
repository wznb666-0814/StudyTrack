package com.repea.studytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repea.studytrack.data.local.entity.ExamWithSubject
import com.repea.studytrack.data.remote.AiMessage
import com.repea.studytrack.data.remote.DeepSeekClient
import com.repea.studytrack.repository.AiChatHistoryRepository
import com.repea.studytrack.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiMessage(
    val role: String, // "user" 或 "assistant"
    val content: String
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val deepSeekClient: DeepSeekClient,
    private val studyRepository: StudyRepository,
    private val historyRepository: AiChatHistoryRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatUiMessage>>(emptyList())
    val messages: StateFlow<List<ChatUiMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 针对当前科目的成绩上下文，供系统提示使用
    private var subjectContext: String? = null

    // 当前对话对应的科目 ID，用于区分不同科目的历史
    private var currentSubjectId: Int? = null

    /**
     * 根据科目 ID 预加载该科目的成绩与排名概要，用于后续对话中作为上下文。
     */
    fun loadSubjectContext(subjectId: Int) {
        viewModelScope.launch {
            try {
                currentSubjectId = subjectId

                // 优先加载历史对话记录
                val history = historyRepository.loadHistory(subjectId)
                if (history.isNotEmpty()) {
                    _messages.value = history
                    return@launch
                }

                val allRecords = studyRepository.getAllRecords().first()
                val subjectRecords = allRecords.filter { it.subject.id == subjectId }
                subjectContext = buildContextFromRecords(subjectRecords)

                if (_messages.value.isEmpty()) {
                    val intro = if (subjectRecords.isNotEmpty()) {
                        val subjectName = subjectRecords.first().subject.name
                        "你好，我是你的 AI 学习分析助手。我已经看过你在「$subjectName」这门课的一些成绩与排名变化，接下来你可以向我咨询解题思路、复习规划、错题整理方法等，我会尽量给出结合你情况的具体建议。"
                    } else {
                        "你好，我是你的 AI 学习分析助手。当前还没有这门课的成绩记录，你依然可以向我咨询学习方法、规划和复习策略。"
                    }
                    _messages.value = listOf(ChatUiMessage(role = "assistant", content = intro))

                    // 初次进入时保存一份“欢迎语”到历史，方便下次直接恢复
                    historyRepository.saveHistory(subjectId, _messages.value)
                }
            } catch (e: Exception) {
                _error.value = "加载科目信息失败，请稍后重试。"
            }
        }
    }

    private fun buildContextFromRecords(records: List<ExamWithSubject>): String {
        if (records.isEmpty()) return "暂时没有该科目的成绩记录。"

        val subjectName = records.first().subject.name
        val fullScore = records.first().subject.fullScore
        val sorted = records.sortedBy { it.exam.examDate }
        val scores = sorted.map { it.exam.score }
        val avg = scores.average()
        val max = scores.maxOrNull() ?: 0.0
        val min = scores.minOrNull() ?: 0.0
        val classRanks = sorted.mapNotNull { it.exam.classRank }
        val gradeRanks = sorted.mapNotNull { it.exam.gradeRank }
        val districtRanks = sorted.mapNotNull { it.exam.districtRank }

        return buildString {
            appendLine("科目：$subjectName，满分：${fullScore.toInt()} 分。")
            appendLine("共有 ${records.size} 次考试记录。")
            appendLine("平均分：${String.format("%.1f", avg)}，最高分：${String.format("%.1f", max)}，最低分：${String.format("%.1f", min)}。")
            if (classRanks.isNotEmpty()) {
                appendLine("班级排名（按时间顺序）：${classRanks.joinToString()}。")
            }
            if (gradeRanks.isNotEmpty()) {
                appendLine("年级排名（按时间顺序）：${gradeRanks.joinToString()}。")
            }
            if (districtRanks.isNotEmpty()) {
                appendLine("区排名（按时间顺序）：${districtRanks.joinToString()}。")
            }
        }
    }

    fun sendMessage(userInput: String) {
        if (userInput.isBlank() || _isSending.value) return

        viewModelScope.launch {
            _isSending.value = true
            _error.value = null

            val trimmed = userInput.trim()
            val currentMessages = _messages.value + ChatUiMessage(role = "user", content = trimmed)
            _messages.value = currentMessages
            currentSubjectId?.let { id ->
                historyRepository.saveHistory(id, _messages.value)
            }

            try {
                val apiMessages = mutableListOf<AiMessage>()

                apiMessages += AiMessage(
                    role = "system",
                    content = "你是一名专业、严谨且友好的学习分析助手，擅长结合学生的成绩与排名变化，为其制定具体可执行的学习策略和复习规划。" +
                        "在回答中请使用简体中文，条理清晰，尽量多给出可落地的学习方法。"
                )

                subjectContext?.let { ctx ->
                    apiMessages += AiMessage(
                        role = "user",
                        content = "以下是这名学生在当前科目中的历史成绩与排名概要，请你在后续对话中记住这些信息并据此回答：\n$ctx"
                    )
                }

                currentMessages.forEach { msg ->
                    apiMessages += AiMessage(role = msg.role, content = msg.content)
                }

                val reply = deepSeekClient.chat(apiMessages)
                _messages.value = _messages.value + ChatUiMessage(role = "assistant", content = reply)
                currentSubjectId?.let { id ->
                    historyRepository.saveHistory(id, _messages.value)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "发送失败，请稍后重试。"
            } finally {
                _isSending.value = false
            }
        }
    }
}

