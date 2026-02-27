package com.repea.studytrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.ui.components.MarkdownText
import com.repea.studytrack.viewmodel.AiChatViewModel
import com.repea.studytrack.viewmodel.ChatUiMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    navController: NavController,
    subjectId: Int,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(subjectId) {
        viewModel.loadSubjectContext(subjectId)
    }

    var input by remember { mutableStateOf("") }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("AI 学习助手", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (error != null) {
                Text(
                    text = error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 对话内容区域使用液态玻璃卡片，自动跟随个性化设置
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = 12.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg ->
                            ChatBubble(message = msg)
                        }

                        if (isSending) {
                            item {
                                ThinkingBubble(
                                    turnIndex = messages.count { it.role == "user" } + 1
                                )
                            }
                        }
                    }
                }
            }

            // 输入区域也包在玻璃卡片中
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 12.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("向 AI 询问学习方法、规划等…") },
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trimmed = input.trim()
                            if (trimmed.isNotEmpty()) {
                                viewModel.sendMessage(trimmed)
                                input = ""
                            }
                        },
                        enabled = !isSending
                    ) {
                        Text(if (isSending) "思考中…" else "发送")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatUiMessage) {
    val isUser = message.role == "user"
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        GlassCard(
            modifier = Modifier
                .widthIn(max = 320.dp),
            contentPadding = 10.dp
        ) {
            if (isUser) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            } else {
                MarkdownText(
                    text = message.content,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun ThinkingBubble(turnIndex: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        GlassCard(
            modifier = Modifier
                .widthIn(max = 320.dp),
            contentPadding = 10.dp
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "第 $turnIndex 轮 · AI 正在思考…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )
            }
        }
    }
}

