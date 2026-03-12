package com.example.firstapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstapplication.ui.theme.MenuVERYBack

@Composable
fun MainWindow(viewModel: MainViewModel = viewModel())
{
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri:Uri? ->
        uri?.let {
            viewModel.transcribeMusic(it,context,"http://10.0.2.2:8000")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(viewModel.isTranscribbing){
            Text("Loading ...")
        }
        else{
            Button(onClick = {launcher.launch("audio/*")}) {
                Text("Pick The music")
            }
        }

        viewModel.transcriptionResult?.let{ notes ->
            Text("Notes received: ${notes.notes.size}")

            if(notes.notes.isNotEmpty()){
                Text("First note: ${notes.notes[0].note}")
            }
        }

    }
}