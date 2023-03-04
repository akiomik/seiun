package io.github.akiomik.seiun

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
import com.example.catpaw.models.LoginParam
import com.example.catpaw.models.Session
import com.example.catpaw.services.AtpService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.akiomik.seiun.ui.theme.SeiunTheme
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.concurrent.thread

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
fun LoginForm(sharedPreferences: SharedPreferences) {
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

                with (sharedPreferences.edit()) {
                    putString("handle", handle)
                    putString("password", password)
                    apply()
                }

                thread {
                    try {
                        val session = getSession(handle, password)
                        Log.d("Seiun", "Login successful")

                        with(sharedPreferences.edit()) {
                            putString("did", session.did)
                            putString("accessJwt", session.accessJwt)
                            apply()
                        }
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
        MyApp()
    }
}

@Composable
private fun MyApp(modifier: Modifier = Modifier) {
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
            LoginForm(sharedPreferences)
        }
    }
}

private fun getSession(handle: String, password: String): Session {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://bsky.social/xrpc/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: AtpService = retrofit.create(AtpService::class.java)
    val session = service.login(
        LoginParam(handle, password)
    ).execute().body()
        ?: throw IllegalStateException("Empty body on login")
    val timeline = service.getTimeline("Bearer ${session.accessJwt}").execute().body()
        ?: throw IllegalStateException("Empty body on getTimeline")

    return session
}