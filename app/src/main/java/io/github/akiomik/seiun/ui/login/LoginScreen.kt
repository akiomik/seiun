package io.github.akiomik.seiun.ui.login

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.ui.theme.Red700
import io.github.akiomik.seiun.viewmodel.LoginViewModel

@Composable
private fun AppName() {
    Text(text = "Seiun", fontSize = 66.sp, modifier = Modifier.padding(8.dp))
}

@Composable
private fun AppDescription() {
    Text(text = "ATP/Bluesky client", fontSize = 33.sp, modifier = Modifier.padding(8.dp))
}

@Composable
private fun LoginTitle() {
    Text(text = "Login", fontSize = 23.sp, modifier = Modifier.padding(20.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginForm(onLoginSuccess: () -> Unit) {
    val viewModel: LoginViewModel = viewModel()
    val (savedHandle, savedPassword) = viewModel.getLoginParam()
    var handle by remember { mutableStateOf(savedHandle) }
    var password by remember { mutableStateOf(savedPassword) }
    var valid by remember { mutableStateOf(handle.isNotEmpty() && password.isNotEmpty()) }
    var errorMessage by remember { mutableStateOf("") }

    Column {
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Red700, modifier = Modifier.padding(20.dp))
        }

        TextField(
            value = handle,
            onValueChange = {
                handle = it
                valid = handle.isNotEmpty() && password.isNotEmpty()
            },
            label = { Text("Handle") },
            placeholder = { Text(text = "jack.bsky.social") },
            maxLines = 1,
            modifier = Modifier.padding(20.dp),
            singleLine = true
        )

        TextField(
            value = password,
            onValueChange = {
                password = it
                valid = handle.isNotEmpty() && password.isNotEmpty()
            },
            label = { Text("Password") },
            maxLines = 1,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.padding(20.dp),
            singleLine = true
        )

        ElevatedButton(
            onClick = {
                Log.d("Seiun", "Login as $handle")

                val userRepository = SeiunApplication.instance!!.userRepository
                userRepository.saveLoginParam(handle, password)

                viewModel.login(
                    handle = handle,
                    password = password,
                    onSuccess = { session ->
                        Log.d("Seiun", "Login successful")
                        userRepository.saveSession(session)
                        onLoginSuccess()
                    },
                    onError = { error ->
                        Log.d("Seiun", "Login failure: $error")
                        errorMessage = error.message ?: "Failed to login"
                    }
                )
            },
            enabled = valid,
            modifier = Modifier.padding(20.dp)

        ) {
            Text("Login")
        }
    }
}

@Composable
private fun CreateAccountButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("or", modifier = Modifier.padding(bottom = 2.dp))
        TextButton(onClick = onClick) {
            Text("Create Account")
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onCreateAccountClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            AppName()
            AppDescription()
            LoginTitle()
            LoginForm(onLoginSuccess)
            Spacer(modifier = Modifier.size(16.dp))
            CreateAccountButton(onCreateAccountClick)
        }
    }
}