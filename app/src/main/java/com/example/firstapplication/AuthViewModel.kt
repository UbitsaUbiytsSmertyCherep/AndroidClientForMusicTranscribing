package com.example.firstapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstapplication.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        // Check existing token
        viewModelScope.launch {
            val token = ServiceLocator.getAuthManager().getToken()
            _isAuthenticated.value = token != null && !token.isBlank()
        }
    }

    fun onEmailChanged(email: String) {
        _email.value = email
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }

    fun onUsernameChanged(username: String) {
        _username.value = username
    }

    fun login() {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            try {
                val request = LoginRequest(_email.value, _password.value)
                val response = ServiceLocator.getApiService().login(request)
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        // Save token (expiration handling simplified in AuthManager)
                        ServiceLocator.getAuthManager().saveAuthResponse(authResponse)
                        _isAuthenticated.value = true
                    } else {
                        _error.value = "Invalid response"
                    }
                } else {
                    _error.value = "Login failed: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            try {
                val request = RegisterRequest(_email.value, _password.value, _username.value)
                val response = ServiceLocator.getApiService().register(request)
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        ServiceLocator.getAuthManager().saveAuthResponse(authResponse)
                        _isAuthenticated.value = true
                    } else {
                        _error.value = "Invalid response"
                    }
                } else {
                    _error.value = "Registration failed: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            ServiceLocator.getAuthManager().clearAuth()
            _isAuthenticated.value = false
            _email.value = ""
            _password.value = ""
            _username.value = ""
        }
    }
}