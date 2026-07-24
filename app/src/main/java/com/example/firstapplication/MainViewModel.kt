package com.example.firstapplication

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.firstapplication.ServiceLocator

class MainViewModel : ViewModel() {

    var isTranscribing by mutableStateOf(false)
        private set

    var transcriptionResult by mutableStateOf<PianoResponse?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedFileName by mutableStateOf<String?>(null)
        private set

    var selectedFileSize by mutableStateOf<String?>(null)
        private set

    private var selectedFileUri: Uri? = null

    var youtubeLink by mutableStateOf("")
        private set

    private var transcriptionsManager: TranscriptionsManager? = null

    private val _savedTranscriptions = MutableStateFlow<List<SavedTranscription>>(emptyList())
    val savedTranscriptions: StateFlow<List<SavedTranscription>> = _savedTranscriptions.asStateFlow()

    var selectedTranscription by mutableStateOf<SavedTranscription?>(null)
        private set

    fun initTranscriptionsManager(context: Context) {
        transcriptionsManager = TranscriptionsManager(context)
        viewModelScope.launch {
            transcriptionsManager?.transcriptionsFlow?.collect { list ->
                _savedTranscriptions.value = list
            }
        }
    }

    fun updateYoutubeLink(link: String) {
        youtubeLink = link
    }

    fun selectFile(uri: Uri, context: Context) {
        selectedFileUri = uri
        transcriptionResult = null
        errorMessage = null

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()

            selectedFileName = cursor.getString(nameIndex)
            val size = cursor.getLong(sizeIndex)
            selectedFileSize = formatFileSize(size)
        }
    }

    fun clearSelectedFile() {
        selectedFileUri = null
        selectedFileName = null
        selectedFileSize = null
        transcriptionResult = null
        errorMessage = null
    }

    fun transcribeFromFile(context: Context, baseUrl: String) {
        val uri = selectedFileUri ?: return
        val fileName = selectedFileName ?: "Unknown file"

        viewModelScope.launch {
            isTranscribing = true
            errorMessage = null

            try {
                val api = ServiceLocator.getApiService()

                val inputStream = context.contentResolver.openInputStream(uri)
                val fileBytes = inputStream?.readBytes() ?: throw Exception("Cannot read file")
                inputStream.close()

                val requestFile = fileBytes.toRequestBody("audio/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", selectedFileName ?: "track.mp3", requestFile)

                val response = api.uploadAudio(body)

                if (response.isSuccessful) {
                    transcriptionResult = response.body()
                    Log.d("Api", "Notes received: ${transcriptionResult?.notes?.size}")

                    transcriptionResult?.let { result ->
                        val savedTranscription = SavedTranscription(
                            fileName = fileName,
                            source = "file",
                            notes = result.notes,
                            tempo = result.tempo,
                            key = result.key,
                            timeSignature = result.timeSignature
                        )
                        transcriptionsManager?.saveTranscription(savedTranscription)
                    }
                } else {
                    errorMessage = "Server error: ${response.code()}"
                    Log.e("Api", "Error: ${response.code()}")
                }

            } catch (e: Exception) {
                if (!e.message.orEmpty().contains("timeout", ignoreCase = true)) {
                    errorMessage = e.message ?: "Unknown error"
                }
                Log.e("Api", "Error: ${e.message}")
            } finally {
                isTranscribing = false
            }
        }
    }

    fun transcribeFromYoutube(baseUrl: String) {
        if (youtubeLink.isBlank()) {
            errorMessage = "Please enter a YouTube link"
            return
        }

        val link = youtubeLink

        viewModelScope.launch {
            isTranscribing = true
            errorMessage = null

            try {
                val api = ServiceLocator.getApiService()
                val response = api.uploadViaLink(Link_Post(link))

                if (response.isSuccessful) {
                    transcriptionResult = response.body()
                    Log.d("Api", "Notes received: ${transcriptionResult?.notes?.size}")

                    transcriptionResult?.let { result ->
                        val savedTranscription = SavedTranscription(
                            fileName = "YouTube: ${extractYoutubeTitle(link)}",
                            source = "youtube",
                            sourceUrl = link,
                            notes = result.notes,
                            tempo = result.tempo,
                            key = result.key,
                            timeSignature = result.timeSignature
                        )
                        transcriptionsManager?.saveTranscription(savedTranscription)
                    }
                } else {
                    errorMessage = "Server error: ${response.code()}"
                    Log.e("Api", "Error: ${response.code()}")
                }

            } catch (e: Exception) {
                if (!e.message.orEmpty().contains("timeout", ignoreCase = true)) {
                    errorMessage = e.message ?: "Unknown error"
                }
                Log.e("Api", "Error: ${e.message}")
            } finally {
                isTranscribing = false
            }
        }
    }

    fun selectTranscription(transcription: SavedTranscription) {
        selectedTranscription = transcription
        transcriptionResult = PianoResponse(
            notes = transcription.notes,
            chords = emptyList(),
            tempo = transcription.tempo ?: 120,
            key = transcription.key ?: "C",
            timeSignature = transcription.timeSignature ?: "4/4"
        )
    }

    fun deleteTranscription(id: String) {
        viewModelScope.launch {
            transcriptionsManager?.deleteTranscription(id)
        }
    }

    fun clearSelectedTranscription() {
        selectedTranscription = null
        transcriptionResult = null
    }

    private fun extractYoutubeTitle(url: String): String {
        val videoIdRegex = Regex("(?:v=|youtu\\.be/)([a-zA-Z0-9_-]{11})")
        val match = videoIdRegex.find(url)
        return match?.groupValues?.get(1) ?: "Video"
    }

    fun clearResult() {
        transcriptionResult = null
        errorMessage = null
    }

    fun clearError() {
        errorMessage = null
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
}