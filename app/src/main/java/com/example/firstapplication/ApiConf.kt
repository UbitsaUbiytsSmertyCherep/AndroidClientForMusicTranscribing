package com.example.firstapplication

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface PianoRollApiService{
    @Multipart
    @POST("/upload")
    suspend fun uploadAudio(
        @Part file: MultipartBody.Part
    ): Response<PianoResponse>

    @POST("/upload/Link")
    suspend fun uploadViaLink(
        @Body request: Link_Post
    ): Response<PianoResponse>

    @POST("/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>
}