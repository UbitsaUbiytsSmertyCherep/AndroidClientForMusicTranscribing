package com.example.firstapplication

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

val Context.transcriptionsDataStore by preferencesDataStore(name = "transcriptions")

private val TRANSCRIPTIONS_KEY = androidx.datastore.preferences.core.stringPreferencesKey("transcriptions_list")

data class SavedTranscription(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val source: String,
    val sourceUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: List<PianoNote>,
    val tempo: Int?,
    val key: String?,
    val timeSignature: String?
)

class TranscriptionsManager(private val context: Context) {
    private val gson = Gson()

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

    suspend fun saveTranscription(transcription: SavedTranscription) {
        context.transcriptionsDataStore.edit { preferences ->
            val currentJson = preferences[TRANSCRIPTIONS_KEY] ?: "[]"
            val type = object : TypeToken<List<SavedTranscription>>() {}.type
            val currentList: MutableList<SavedTranscription> = try {
                gson.fromJson(currentJson, type) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            currentList.add(0, transcription)

            val trimmedList = currentList.take(50)

            preferences[TRANSCRIPTIONS_KEY] = gson.toJson(trimmedList)
        }
    }

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

    suspend fun clearAll() {
        context.transcriptionsDataStore.edit { preferences ->
            preferences[TRANSCRIPTIONS_KEY] = "[]"
        }
    }
}