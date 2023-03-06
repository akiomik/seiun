package io.github.akiomik.seiun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.akiomik.seiun.ui.login.LoginScreen
import io.github.akiomik.seiun.ui.registration.RegistrationScreen
import io.github.akiomik.seiun.ui.theme.SeiunTheme
import io.github.akiomik.seiun.ui.timeline.TimelineScreen

class SeiunActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    SeiunTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "login") {
            composable("timeline") { TimelineScreen(/*...*/) }
            composable("login") {
                LoginScreen(onLoginSuccess = {
                    navController.navigate("timeline")
                }, onCreateAccountClick = {
                    navController.navigate("registration")
                })
            }
            composable("registration") {
                RegistrationScreen(onRegistrationSuccess = {
                    navController.navigate("timeline")
                })
            }
        }
    }
}