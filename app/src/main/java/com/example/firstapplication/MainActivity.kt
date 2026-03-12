package com.example.firstapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.firstapplication.ui.theme.FirstApplicationTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding->
                    //InnerScreen(modifier = Modifier.padding(innerPadding))
                    //SideBar(modifier = Modifier.padding(innerPadding))
                    NavBar(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FirstApplicationTheme {
        Greeting("Android")
    }
}

@Preview
@Composable
fun BarPreview(){
    FirstApplicationTheme {
        NavBar();
    }
}


@Composable
fun InnerScreen(modifier: Modifier = Modifier){
    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(text=message,style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = message,
            onValueChange = {message = it},
            label = {Text("Enter Name")}
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            message = if (name.isNotBlank()) "Hi $name!" else "Enter Name"
            Log.d("MY_TAG", "Значение переменной name: $name")
        }){
            Text("HI")
        }

    }


}