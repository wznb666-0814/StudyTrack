package com.repea.studytrack.data.remote

import com.repea.studytrack.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

data class AiMessage(
    val role: String,
    val content: String
)

@Singleton
class DeepSeekClient @Inject constructor(
    private val client: OkHttpClient
) {

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
    private val baseUrl: String = BuildConfig.DEEPSEEK_BASE_URL.trimEnd('/')
    private val model: String = BuildConfig.DEEPSEEK_MODEL

    private fun apiKey(): String {
        return "sk-cf0a235045c64c819d66363755981f41"
    }

    private fun buildRequestBody(messages: List<AiMessage>): String {
        val root = JSONObject()
        root.put("model", model)
        root.put("stream", false)

        val messagesArray = JSONArray()
        messages.forEach { msg ->
            val obj = JSONObject()
            obj.put("role", msg.role)
            obj.put("content", msg.content)
            messagesArray.put(obj)
        }
        root.put("messages", messagesArray)

        return root.toString()
    }

    private fun parseContent(body: String): String {
        return try {
            val root = JSONObject(body)
            val choices = root.optJSONArray("choices") ?: return ""
            if (choices.length() == 0) return ""
            val first = choices.getJSONObject(0)
            val message = first.getJSONObject("message")
            message.optString("content", "")
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 通用对话接口封装，直接返回模型回答文本。
     */
    suspend fun chat(messages: List<AiMessage>): String {
        val key = apiKey()

        val url = "$baseUrl/chat/completions"
        val jsonBody = buildRequestBody(messages)

        suspend fun executeOnce(): String = withContext(Dispatchers.IO) {
            val requestBody = jsonBody.toRequestBody(mediaTypeJson)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $key")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    // 尝试从返回体中解析更友好的错误信息
                    val friendly = try {
                        val root = JSONObject(bodyStr)
                        val error = root.optJSONObject("error")
                        val msg = error?.optString("message")?.takeIf { it.isNotBlank() }
                        msg ?: "HTTP ${response.code}"
                    } catch (_: Exception) {
                        "HTTP ${response.code}"
                    }
                    throw IllegalStateException("DeepSeek 调用失败：$friendly")
                }

                val content = parseContent(bodyStr)
                if (content.isBlank()) {
                    throw IllegalStateException("DeepSeek 返回内容为空")
                }
                content
            }
        }

        // 简单的超时重试：遇到 SocketTimeoutException 时自动重试一次
        return try {
            executeOnce()
        } catch (e: SocketTimeoutException) {
            // 第二次再超时就直接抛给上层，让 UI 提示“请求超时”
            try {
                executeOnce()
            } catch (e2: SocketTimeoutException) {
                throw IllegalStateException("DeepSeek 请求超时，请检查网络后重试。")
            }
        }
    }

    /**
     * 针对单科成绩分析的专用封装。
     */
    suspend fun generateSubjectAdvice(subjectPrompt: String): String {
        val system = AiMessage(
            role = "system",
            content = "你是一名专业、严谨且友好的学习分析助手，擅长根据学生的成绩趋势和排名变化给出具体可执行的学习建议。" +
                "回答时使用简体中文，语气鼓励但不鸡汤，尽量给出可落地的学习方法、时间规划和复习策略。"
        )
        val user = AiMessage(
            role = "user",
            content = subjectPrompt
        )
        return chat(listOf(system, user))
    }
}

