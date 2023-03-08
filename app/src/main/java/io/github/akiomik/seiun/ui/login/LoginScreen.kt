package io.github.akiomik.seiun.ui.login

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.ui.theme.Red700
import io.github.akiomik.seiun.viewmodel.LoginViewModel

@Composable
private fun AppName() {
    Text(
        text = stringResource(id = R.string.app_name),
        fontSize = 66.sp,
        modifier = Modifier.padding(top = 20.dp, end = 20.dp, bottom = 8.dp, start = 20.dp)
    )
}

@Composable
private fun AppDescription() {
    Text(
        text = stringResource(id = R.string.login_app_description),
        fontSize = 33.sp,
        modifier = Modifier.padding(top = 8.dp, end = 20.dp, bottom = 8.dp, start = 20.dp)
    )
}

@Composable
private fun LoginTitle() {
    Text(
        text = stringResource(id = R.string.login_title),
        fontSize = 23.sp,
        modifier = Modifier.padding(20.dp)
    )
}

@Composable
private fun LoginForm(onLoginSuccess: () -> Unit) {
    val viewModel: LoginViewModel = viewModel()
    val (savedHandleOrEmail, savedPassword) = viewModel.getLoginParam()
    var handleOrEmail by remember { mutableStateOf(savedHandleOrEmail) }
    var password by remember { mutableStateOf(savedPassword) }
    var valid by remember { mutableStateOf(handleOrEmail.isNotEmpty() && password.isNotEmpty()) }
    var errorMessage by remember { mutableStateOf("") }
    val loginErrorMessage = stringResource(id = R.string.login_error)

    Column {
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Red700, modifier = Modifier.padding(20.dp))
        }

        TextField(
            value = handleOrEmail,
            onValueChange = {
                handleOrEmail = it
                valid = handleOrEmail.isNotEmpty() && password.isNotEmpty()
            },
            label = { Text(stringResource(id = R.string.login_handle_or_email)) },
            placeholder = { Text(text = stringResource(id = R.string.login_handle_or_email_placeholder)) },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            singleLine = true
        )

        TextField(
            value = password,
            onValueChange = {
                password = it
                valid = handleOrEmail.isNotEmpty() && password.isNotEmpty()
            },
            label = { Text(stringResource(id = R.string.login_password)) },
            maxLines = 1,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            singleLine = true
        )

        ElevatedButton(
            onClick = {
                Log.d(SeiunApplication.TAG, "Login as $handleOrEmail")

                val userRepository = SeiunApplication.instance!!.userRepository
                userRepository.saveLoginParam(handleOrEmail, password)

                viewModel.login(
                    handle = handleOrEmail,
                    password = password,
                    onSuccess = { session ->
                        Log.d(SeiunApplication.TAG, "Login successful")
                        userRepository.saveSession(session)
                        onLoginSuccess()
                    },
                    onError = { error ->
                        Log.d(SeiunApplication.TAG, "Login failure: $error")
                        errorMessage = error.message ?: loginErrorMessage
                    }
                )
            },
            enabled = valid,
            modifier = Modifier.padding(20.dp)

        ) {
            Text(stringResource(id = R.string.login_button))
        }
    }
}

@Composable
private fun CreateAccountButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(id = R.string.login_or), modifier = Modifier.padding(bottom = 2.dp))
        TextButton(onClick = onClick) {
            Text(stringResource(id = R.string.login_create_account))
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