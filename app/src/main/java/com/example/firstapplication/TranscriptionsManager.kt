package com.example.firstapplication

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

// Extension для DataStore
val Context.transcriptionsDataStore by preferencesDataStore(name = "transcriptions")

// Ключ для хранения JSON
private val TRANSCRIPTIONS_KEY = androidx.datastore.preferences.core.stringPreferencesKey("transcriptions_list")

// Модель сохранённой транскрипции
data class SavedTranscription(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val source: String, // "file" or "youtube"
    val sourceUrl: String? = null, // YouTube ссылка если есть
    val timestamp: Long = System.currentTimeMillis(),
    val notes: List<PianoNote>,
    val tempo: Int?,
    val key: String?,
    val timeSignature: String?
)

class TranscriptionsManager(private val context: Context) {
    private val gson = Gson()

    // Flow со списком транскрипций
    val transcriptionsFlow: Flow<List<SavedTranscription>> = context.transcriptionsDataStore.data
        .map { preferences ->
            val json = preferences[TRANSCRIPTIONS_KEY] ?: "[]"
            try {
                val type = object : TypeToken<List<SavedTranscription>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    // Сохранить новую транскрипцию
    suspend fun saveTranscription(transcription: SavedTranscription) {
        context.transcriptionsDataStore.edit { preferences ->
            val currentJson = preferences[TRANSCRIPTIONS_KEY] ?: "[]"
            val type = object : TypeToken<List<SavedTranscription>>() {}.type
            val currentList: MutableList<SavedTranscription> = try {
                gson.fromJson(currentJson, type) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            // Добавляем в начало списка (новые сверху)
            currentList.add(0, transcription)

            // Ограничиваем список до 50 записей
            val trimmedList = currentList.take(50)

            preferences[TRANSCRIPTIONS_KEY] = gson.toJson(trimmedList)
        }
    }

    // Удалить транскрипцию
    suspend fun deleteTranscription(id: String) {
        context.transcriptionsDataStore.edit { preferences ->
            val currentJson = preferences[TRANSCRIPTIONS_KEY] ?: "[]"
            val type = object : TypeToken<List<SavedTranscription>>() {}.type
            val currentList: MutableList<SavedTranscription> = try {
                gson.fromJson(currentJson, type) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            val updatedList = currentList.filter { it.id != id }
            preferences[TRANSCRIPTIONS_KEY] = gson.toJson(updatedList)
        }
    }

    // Очистить все транскрипции
    suspend fun clearAll() {
        context.transcriptionsDataStore.edit { preferences ->
            preferences[TRANSCRIPTIONS_KEY] = "[]"
        }
    }
}