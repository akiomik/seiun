package io.github.akiomik.seiun.ui.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.ui.login.LoginScreen
import io.github.akiomik.seiun.ui.notification.NotificationScreen
import io.github.akiomik.seiun.ui.registration.RegistrationScreen
import io.github.akiomik.seiun.ui.timeline.TimelineScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier,
    timelineListState: LazyListState,
    notificationListState: LazyListState,
    startDestination: String
) {
    val application = SeiunApplication.instance!!
    val context = LocalContext.current

    val isNotificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val postNotificationPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        postNotificationPermission == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            Log.d(SeiunApplication.TAG, "Queue notification worker")
            application.registerNotificationWorker()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("timeline") {
            if (!application.isAtpServiceInitialized()) {
                application.setAtpClient()
            }

            // NOTE: Register notification worker when already granted
            if (isNotificationGranted) {
                Log.d(SeiunApplication.TAG, "Queue notification worker")
                application.registerNotificationWorker()
            }

            TimelineScreen(timelineListState)
        }
        composable("notification") {
            if (!application.isAtpServiceInitialized()) {
                application.setAtpClient()
            }

            application.clearNotifications()

            // NOTE: Request permission and register notification worker when granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d(SeiunApplication.TAG, "Queue notification worker")
                application.registerNotificationWorker()
            }

            NotificationScreen(notificationListState)
        }
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("timeline")
            }, onCreateAccountClick = {
                    navController.navigate("registration")
                })
        }
        composable("registration") {
            RegistrationScreen(onRegistrationSuccess = { navController.navigate("timeline") })
        }
    }
}
