package com.repea.studytrack.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.repea.studytrack.viewmodel.ChatUiMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private const val DATA_STORE_NAME = "ai_chat_history"

private val Context.aiChatDataStore by preferencesDataStore(name = DATA_STORE_NAME)

@Singleton
class AiChatHistoryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun keyForSubject(subjectId: Int): Preferences.Key<String> =
        stringPreferencesKey("subject_$subjectId")

    suspend fun loadHistory(subjectId: Int): List<ChatUiMessage> {
        val key = keyForSubject(subjectId)
        val json = context.aiChatDataStore.data
            .map { prefs -> prefs[key] }
            .first()
            ?: return emptyList()

        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        ChatUiMessage(
                            role = obj.optString("role", "assistant"),
                            content = obj.optString("content", "")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    suspend fun saveHistory(subjectId: Int, messages: List<ChatUiMessage>) {
        val key = keyForSubject(subjectId)
        val array = JSONArray().apply {
            messages.forEach { msg ->
                val obj = JSONObject()
                obj.put("role", msg.role)
                obj.put("content", msg.content)
                put(obj)
            }
        }

        context.aiChatDataStore.edit { prefs ->
            prefs[key] = array.toString()
        }
    }
}

