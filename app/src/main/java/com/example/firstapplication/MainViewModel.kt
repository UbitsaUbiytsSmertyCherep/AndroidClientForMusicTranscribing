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
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {

    var isTranscribing by mutableStateOf(false)
        private set

    var transcriptionResult by mutableStateOf<PianoResponse?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Для отображения информации о выбранном файле
    var selectedFileName by mutableStateOf<String?>(null)
        private set

    var selectedFileSize by mutableStateOf<String?>(null)
        private set

    private var selectedFileUri: Uri? = null

    // Для YouTube ссылки
    var youtubeLink by mutableStateOf("")
        private set

    fun updateYoutubeLink(link: String) {
        youtubeLink = link
    }

    fun selectFile(uri: Uri, context: Context) {
        selectedFileUri = uri
        transcriptionResult = null
        errorMessage = null

        // Получаем информацию о файле
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

        viewModelScope.launch {
            isTranscribing = true
            errorMessage = null

            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(PianoRollApiService::class.java)

                val inputStream = context.contentResolver.openInputStream(uri)
                val fileBytes = inputStream?.readBytes() ?: throw Exception("Cannot read file")
                inputStream.close()

                val requestFile = fileBytes.toRequestBody("audio/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", selectedFileName ?: "track.mp3", requestFile)

                val response = api.uploadAudio(body)

                if (response.isSuccessful) {
                    transcriptionResult = response.body()
                    Log.d("Api", "Notes received: ${transcriptionResult?.notes?.size}")
                } else {
                    errorMessage = "Server error: ${response.code()}"
                    Log.e("Api", "Error: ${response.code()}")
                }

            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error"
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

        viewModelScope.launch {
            isTranscribing = true
            errorMessage = null

            try {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient) // Подключаем настроенный клиент
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()


                val api = retrofit.create(PianoRollApiService::class.java)

                val response = api.uploadViaLink(Link_Post(youtubeLink))

                if (response.isSuccessful) {
                    transcriptionResult = response.body()
                    Log.d("Api", "Notes received: ${transcriptionResult?.notes?.size}")
                } else {
                    errorMessage = "Server error: ${response.code()}"
                    Log.e("Api", "Error: ${response.code()}")
                }

            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error"
                Log.e("Api", "Error: ${e.message}")
            } finally {
                isTranscribing = false
            }
        }
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