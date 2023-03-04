package io.github.akiomik.seiun

import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.security.crypto.MasterKey
import androidx.security.crypto.EncryptedSharedPreferences
import io.github.akiomik.seiun.model.LoginParam
import io.github.akiomik.seiun.model.Session
import com.example.catpaw.services.AtpService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.akiomik.seiun.ui.theme.SeiunTheme
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.concurrent.thread

class RootActivity : ComponentActivity() {
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

@Composable
fun LoginForm(sharedPreferences: SharedPreferences, onLoginSuccessful: () -> Unit) {
    val savedHandle = sharedPreferences.getString("handle", "") ?: ""
    val savedPassword = sharedPreferences.getString("password", "") ?: ""

    var handle by remember { mutableStateOf(savedHandle) }
    var password by remember { mutableStateOf(savedPassword) }
    var valid by remember { mutableStateOf(handle.isNotEmpty() && password.isNotEmpty()) }

    Column {
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

        ElevatedButton(
            onClick = {
                Log.d("login", "as $handle")

                val userRepository = SeiunApplication.instance!!.userRepository
                userRepository.saveLoginParam(handle, password)

                thread {
                    try {
                        val session = userRepository.login(handle, password)
                        Log.d("Seiun", "Login successful")

                        userRepository.saveSession(session)
                        onLoginSuccessful()
                    } catch (e: java.lang.Exception) {
                        Log.d("Seiun", "Failed to login $e")
                    }
                }
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
        MyApp({})
    }
}

@Composable
private fun MyApp(onLoginSuccessful: () -> Unit, modifier: Modifier = Modifier) {
    val key = MasterKey.Builder(LocalContext.current)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val sharedPreferences = EncryptedSharedPreferences.create(
        LocalContext.current,
        "seiun",
        key,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            AppName()
            AppDescription()
            LoginTitle()
            LoginForm(sharedPreferences, onLoginSuccessful)
        }
    }
}