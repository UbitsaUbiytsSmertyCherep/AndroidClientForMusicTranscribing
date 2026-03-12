package com.example.firstapplication

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application){
    private val _apiEndpoint = mutableStateOf("http://10.0.2.2:8000")
    val apiEndpoint: State<String> = _apiEndpoint
    private val _saveLocation = mutableStateOf("Not Selected")
    val saveLocation: State<String> = _saveLocation

    private val settingsManager = SettingsManager(application)

    init {
        viewModelScope.launch {
            settingsManager.apiEndpointFlow.collect { savedUrl->_apiEndpoint.value = savedUrl }
        }
    }

    fun updateApiEndpoint(newUrl:String){
        _apiEndpoint.value = newUrl
    }
    fun UpdateSaveLocation(newPath: String){
        _saveLocation.value = newPath
    }

    fun saveSettings(){
        viewModelScope.launch {
            settingsManager.saveApiEndpoint(_apiEndpoint.value)
            Log.d("SETTINGS", "Data has been saved")
        }
    }

}