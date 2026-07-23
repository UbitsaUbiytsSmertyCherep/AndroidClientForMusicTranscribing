package com.example.firstapplication

import android.app.Application
import android.content.Context

object ServiceLocator {
    private var application: Application? = null
    private var authManager: AuthManager? = null
    private var apiClient: ApiClient? = null

    fun init(app: Application) {
        application = app
        authManager = AuthManager(app)
        apiClient = ApiClient.create(app, authManager!!)
    }

    fun getAuthManager(): AuthManager {
        return authManager ?: throw IllegalStateException("ServiceLocator not initialized")
    }

    fun getApiClient(): ApiClient {
        return apiClient ?: throw IllegalStateException("ServiceLocator not initialized")
    }

    fun getApiService(): PianoRollApiService {
        return apiClient?.apiService
            ?: throw IllegalStateException("ApiClient not initialized")
    }

    fun getContext(): Context {
        return application ?: throw IllegalStateException("ServiceLocator not initialized")
    }
}