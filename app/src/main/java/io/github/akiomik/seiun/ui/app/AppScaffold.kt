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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.ui.timeline.NewPostFab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(from: String?, drawerState: DrawerState) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val timelineListState = rememberLazyListState()
    val notificationListState = rememberLazyListState()
    var topBarState by rememberSaveable { (mutableStateOf(false)) }
    var bottomBarState by rememberSaveable { (mutableStateOf(false)) }
    var fabState by remember { (mutableStateOf<@Composable () -> Unit>({})) }
    val atpService by SeiunApplication.instance!!.atpService.collectAsState()

    val startDestination =
        if (from == "notification" && atpService != null) {
            "notification"
        } else {
            "login"
        }

    when (navBackStackEntry?.destination?.route) {
        "timeline" -> {
            topBarState = true
            bottomBarState = true
            fabState = { NewPostFab() }
        }
        "notification" -> {
            topBarState = true
            bottomBarState = true
            fabState = {}
        }
        else -> {
            Log.d(SeiunApplication.TAG, "Cancel all work")
            WorkManager.getInstance(LocalContext.current).cancelAllWork()

            topBarState = false
            bottomBarState = false
            fabState = {}
        }
    }

    Scaffold(
        topBar = { AppTopBar(scrollBehavior, visible = topBarState, drawerState = drawerState) },
        bottomBar = {
            AppBottomBar(
                navController = navController,
                visible = bottomBarState,
                timelineListState = timelineListState,
                notificationListState = notificationListState
            )
        },
        floatingActionButton = fabState,
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
