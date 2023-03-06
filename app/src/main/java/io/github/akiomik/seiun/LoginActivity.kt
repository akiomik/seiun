package io.github.akiomik.seiun

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.ui.theme.SeiunTheme
import io.github.akiomik.seiun.viewmodel.LoginViewModel

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val moveToMain = {
                Log.d("Seiun", "Move Root to Main")
                val intent = Intent(application, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                applicationContext.startActivity(intent)
            }

            SeiunTheme {
                MyApp(modifier = Modifier.fillMaxSize(), onLoginSuccessful =  moveToMain)
            }
        }
    }
}

@Composable
fun AppName() {
    Text(text = "Seiun", fontSize = 66.sp, modifier = Modifier.padding(8.dp))
}

@Composable
fun AppDescription() {
    Text(text = "ATP/Bluesky client", fontSize = 33.sp, modifier = Modifier.padding(8.dp))
}

@Composable
fun LoginTitle() {
    Text(text = "Login", fontSize = 23.sp, modifier = Modifier.padding(20.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(onLoginSuccessful: () -> Unit) {
    val viewModel: LoginViewModel = viewModel()
    val (savedHandle, savedPassword) = viewModel.getLoginParam()
    var handle by remember { mutableStateOf(savedHandle) }
    var password by remember { mutableStateOf(savedPassword) }
    var valid by remember { mutableStateOf(handle.isNotEmpty() && password.isNotEmpty()) }
    var errorMessage by remember { mutableStateOf("") }

    Column {
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = colorResource(id = R.color.red_700), modifier = Modifier.padding(20.dp))
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
            modifier = Modifier.padding(20.dp)
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
            modifier = Modifier.padding(20.dp)
        )

        val context = LocalContext.current
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
                        if (session != null) {
                            userRepository.saveSession(session)
                            onLoginSuccessful()
                        }
                    },
                    onError = { error ->
                        Log.d("Seiun", "Login failure: ${error.toString()}")
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
private fun MyApp(onLoginSuccessful: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            AppName()
            AppDescription()
            LoginTitle()
            LoginForm(onLoginSuccessful)
        }
    }
}