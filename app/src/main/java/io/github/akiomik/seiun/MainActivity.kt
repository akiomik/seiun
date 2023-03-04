package io.github.akiomik.seiun

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.akiomik.seiun.ui.theme.SeiunTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeiunTheme {
                MyApp(modifier = Modifier.fillMaxSize())
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

@Composable
fun LoginForm() {
    var handle by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var valid by remember { mutableStateOf(false) }

    Column {
        TextField(
            value = handle,
            onValueChange = {
                handle = it;
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

        ElevatedButton(
            onClick = {
                // TODO
                Log.d("hoge", "$handle:$password")
            },
            enabled = valid,
            modifier = Modifier.padding(20.dp)
        ) {
            Text("Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SeiunTheme {
        MyApp()
    }
}

@Composable
private fun MyApp(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            AppName()
            AppDescription()
            LoginTitle()
            LoginForm()
        }
    }
}