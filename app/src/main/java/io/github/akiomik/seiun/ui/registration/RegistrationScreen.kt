package io.github.akiomik.seiun.ui.registration

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.ui.theme.Red700
import io.github.akiomik.seiun.viewmodel.RegistrationViewModel

@Composable
fun RegistrationTitle() {
    Text(text = "Create Account", fontSize = 23.sp, modifier = Modifier.padding(20.dp))
}

@Composable
fun RegistrationForm(onRegistrationSuccess: () -> Unit) {
    val viewModel: RegistrationViewModel = viewModel()
    var email by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    val valid =
        email.isNotEmpty() && handle.isNotEmpty() && password.isNotEmpty() && inviteCode.isNotEmpty()
    var errorMessage by remember { mutableStateOf("") }

    Column {
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Red700, modifier = Modifier.padding(20.dp))
        }

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text(text = "jack@example.com") },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.padding(20.dp),
            singleLine = true
        )

        TextField(
            value = handle,
            onValueChange = { handle = it },
            label = { Text("Handle") },
            placeholder = { Text(text = "jack") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            prefix = { Text(text = "@") },
            suffix = { Text(text = ".bsky.social") },
            maxLines = 1,
            modifier = Modifier.padding(20.dp),
            singleLine = true
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            maxLines = 1,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.padding(20.dp),
            singleLine = true
        )

        TextField(
            value = inviteCode,
            onValueChange = { inviteCode = it },
            label = { Text("Invite code") },
            placeholder = { Text(text = "bsky.social-XXXXXX") },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier = Modifier.padding(20.dp),
            singleLine = true
        )

        ElevatedButton(
            onClick = {
                Log.d(SeiunApplication.TAG, "Login as $handle")

                val userRepository = SeiunApplication.instance!!.userRepository
                userRepository.saveLoginParam(handle, password)

                viewModel.register(
                    email = email,
                    handle = "${handle}.bsky.social",
                    password = password,
                    inviteCode = inviteCode,
                    onSuccess = { session ->
                        Log.d(SeiunApplication.TAG, "Login successful")
                        userRepository.saveSession(session)
                        onRegistrationSuccess()
                    },
                    onError = { error ->
                        Log.d(SeiunApplication.TAG, "Login failure: $error")
                        errorMessage = error.message ?: "Failed to login"
                    }
                )
            },
            enabled = valid,
            modifier = Modifier.padding(20.dp)
        ) {
            Text("Create")
        }
    }
}

@Composable
fun RegistrationScreen(onRegistrationSuccess: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            RegistrationTitle()
            RegistrationForm(onRegistrationSuccess = onRegistrationSuccess)
        }
    }
}

