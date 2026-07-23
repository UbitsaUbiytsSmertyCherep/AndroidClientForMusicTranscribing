package com.example.firstapplication


import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstapplication.ui.theme.FirstApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val username by viewModel.username.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    // Notify parent when authentication state changes to true
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onAuthSuccess()
        }
    }

    FirstApplicationTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text("Authentication") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Welcome to NoteTranscriber",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 24.dp)
                )

                // Simple login/register switch
                var showLogin by remember { mutableStateOf(true) }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = { showLogin = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Login", color = if (showLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                    TextButton(
                        onClick = { showLogin = false },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Register", color = if (!showLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showLogin) {
                    LoginForm(
                        email = email,
                        password = password,
                        onEmailChanged = { viewModel.onEmailChanged(it) },
                        onPasswordChanged = { viewModel.onPasswordChanged(it) },
                        onLogin = { viewModel.login() },
                        isLoading = isLoading,
                        error = error
                    )
                } else {
                    RegisterForm(
                        email = email,
                        password = password,
                        username = username,
                        onEmailChanged = { viewModel.onEmailChanged(it) },
                        onPasswordChanged = { viewModel.onPasswordChanged(it) },
                        onUsernameChanged = { viewModel.onUsernameChanged(it) },
                        onRegister = { viewModel.register() },
                        isLoading = isLoading,
                        error = error
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginForm(
    email: String,
    password: String,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TextField(
            value = email,
            onValueChange = onEmailChanged,
            label = { Text("Email") },
            placeholder = { Text("Enter email") },

            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextField(
            value = password,
            onValueChange = onPasswordChanged,
            label = { Text("Password") },
            placeholder = { Text("Enter password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onLogin,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Login")
            }
        }
    }
}

@Composable
private fun RegisterForm(
    email: String,
    password: String,
    username: String,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onRegister: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TextField(
            value = email,
            onValueChange = onEmailChanged,
            label = { Text("Email") },
            placeholder = { Text("Enter email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextField(
            value = password,
            onValueChange = onPasswordChanged,
            label = { Text("Password") },
            placeholder = { Text("Enter password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextField(
            value = username,
            onValueChange = onUsernameChanged,
            label = { Text("Username") },
            placeholder = { Text("Enter username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRegister,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Register")
            }
        }
    }
}