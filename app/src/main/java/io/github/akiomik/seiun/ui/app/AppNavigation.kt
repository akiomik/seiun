package io.github.akiomik.seiun.ui.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.ui.login.LoginScreen
import io.github.akiomik.seiun.ui.notification.NotificationScreen
import io.github.akiomik.seiun.ui.registration.RegistrationScreen
import io.github.akiomik.seiun.ui.timeline.TimelineScreen
import io.github.akiomik.seiun.ui.user.UserScreen
import io.github.akiomik.seiun.viewmodels.AppViewModel
import io.github.akiomik.seiun.viewmodels.FollowersViewModel
import io.github.akiomik.seiun.viewmodels.FollowsViewModel
import io.github.akiomik.seiun.viewmodels.UserFeedViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier,
    timelineListState: LazyListState,
    notificationListState: LazyListState,
    startDestination: String
) {
    val application = SeiunApplication.instance!!
    val atpService by application.atpService.collectAsState()
    val viewModel: AppViewModel = viewModel()
    val profile by viewModel.profile.collectAsState()
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
            if (atpService == null) {
                application.setAtpClient()
            }

            if (profile == null) {
                viewModel.updateProfile()
            }

            // NOTE: Register notification worker when already granted
            if (isNotificationGranted) {
                Log.d(SeiunApplication.TAG, "Queue notification worker")
                application.registerNotificationWorker()
            }

            TimelineScreen(
                timelineListState,
                onProfileClick = { navController.navigate("user/$it") }
            )
        }
        composable("notification") {
            if (atpService == null) {
                application.setAtpClient()
            }

            if (profile == null) {
                viewModel.updateProfile()
            }

            application.clearNotifications()

            // NOTE: Request permission and register notification worker when granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d(SeiunApplication.TAG, "Queue notification worker")
                application.registerNotificationWorker()
            }

            NotificationScreen(
                notificationListState,
                onProfileClick = { navController.navigate("user/$it") }
            )
        }
        composable(
            "user/{did}",
            arguments = listOf(navArgument("did") { type = NavType.StringType })
        ) {
            val did = it.arguments?.getString("did")!!

            val userFeedViewModel: UserFeedViewModel = viewModel(
                extras = MutableCreationExtras().apply { set(UserFeedViewModel.didKey, did) },
                factory = UserFeedViewModel.Factory
            )
            val followsViewModel: FollowsViewModel = viewModel(
                extras = MutableCreationExtras().apply { set(FollowsViewModel.didKey, did) },
                factory = FollowsViewModel.Factory
            )
            val followersViewModel: FollowersViewModel = viewModel(
                extras = MutableCreationExtras().apply { set(FollowersViewModel.didKey, did) },
                factory = FollowersViewModel.Factory
            )

            UserScreen(
                userFeedViewModel = userFeedViewModel,
                followsViewModel = followsViewModel,
                followersViewModel = followersViewModel,
                onProfileClick = { otherDid -> navController.navigate("user/$otherDid") }
            )
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
