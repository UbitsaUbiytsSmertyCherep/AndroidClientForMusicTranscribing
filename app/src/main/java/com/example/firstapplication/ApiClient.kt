package com.example.firstapplication

import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient private constructor(
    private val context: Context,
    private val authManager: AuthManager
) {
    private val baseUrl by lazy {
        "http://10.0.2.2:8000"
    }

    val apiService: PianoRollApiService
        get() = createService()

    private fun createService(): PianoRollApiService {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                val token = runBlocking { authManager.getToken() }
                if (token != null) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                val request = requestBuilder.build()
                return@addInterceptor chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PianoRollApiService::class.java)
    }

    companion object {
        fun create(context: Context, authManager: AuthManager): ApiClient {
            return ApiClient(context, authManager)
        }
    }
}