package com.example.firstapplication

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI

class MainViewModel: ViewModel(){
    var isTranscribbing by mutableStateOf(false)
    var transcriptionResult by mutableStateOf<PianoResponse?>(null)

    fun transcribeMusic(uri: Uri, context: Context, baseURl: String){
        val retrofit = Retrofit.Builder()
            .baseUrl(baseURl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(PianoRollApiService::class.java)

        viewModelScope.launch {
            isTranscribbing = true
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileBytes = inputStream?.readBytes() ?: throw Exception("Cant Read File")
                inputStream.close()

                val requestFile = fileBytes.toRequestBody("audio/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", "track.mp3", requestFile)

                val response = api.uploadAudio(body)

                if (response.isSuccessful){
                    transcriptionResult = response.body()
                    Log.d("Api", "Notes Recieved: ${transcriptionResult?.notes?.size}")

                }
                else{
                    Log.e("Api", "The Error occurred during handling of response")
                }



            }
            catch (e: Exception){
                Log.e("Api", "API NOT WORKING: ${e.message}")
            }
            finally {
                isTranscribbing = false
            }
        }
    }
}