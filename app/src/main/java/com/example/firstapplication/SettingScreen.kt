package com.example.firstapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingScreen(settingsViewModel: SettingsViewModel = viewModel()){
    val url by settingsViewModel.apiEndpoint
    val path by settingsViewModel.saveLocation

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally)
    {
        OutlinedTextField(
            value = url,
            onValueChange = {settingsViewModel.updateApiEndpoint(it)},
            label = { Text("Api Endpoint") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Text(text = "Save Location: $path")

        Spacer(Modifier.height(16.dp))

        Button( onClick = {settingsViewModel.saveSettings()}) {
            Text("Save")
        }

    }
}