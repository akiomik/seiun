package io.github.akiomik.seiun.ui.app

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.work.WorkManager
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.viewmodels.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(from: String?, drawerState: DrawerState, navController: NavHostController) {
    val viewModel: AppViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val timelineListState = rememberLazyListState()
    val notificationListState = rememberLazyListState()
    val atpService by SeiunApplication.instance!!.atpService.collectAsState()
    val showTopBar by viewModel.showTopBar.collectAsState()
    val showBottomBar by viewModel.showBottomBar.collectAsState()
    val fab by viewModel.fab.collectAsState()

    val startDestination =
        if (from == "notification" && atpService != null) {
            "notification"
        } else {
            "login"
        }

    val route = navBackStackEntry?.destination?.route
    val matchUser = route?.startsWith("user/", true) ?: false
    when {
        route == "timeline" -> {
            viewModel.onTimeline()
        }
        route == "notification" -> {
            viewModel.onNotification()
        }
        matchUser -> {
            viewModel.onUser()
        }
        else -> {
            Log.d(SeiunApplication.TAG, "Cancel all work")
            WorkManager.getInstance(LocalContext.current).cancelAllWork()

            viewModel.onLoginOrRegistration()
        }
    }

    Scaffold(
        topBar = { AppTopBar(scrollBehavior, visible = showTopBar, drawerState = drawerState) },
        bottomBar = {
            AppBottomBar(
                navController = navController,
                visible = showBottomBar,
                timelineListState = timelineListState,
                notificationListState = notificationListState
            )
        },
        floatingActionButton = fab,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = {
            AppNavigation(
                navController = navController,
                modifier = Modifier.padding(it),
                timelineListState = timelineListState,
                notificationListState = notificationListState,
                startDestination = startDestination
            )
        }
    )
}
