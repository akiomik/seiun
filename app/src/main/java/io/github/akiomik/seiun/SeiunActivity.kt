package io.github.akiomik.seiun

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import coil.compose.AsyncImage
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.ui.login.LoginScreen
import io.github.akiomik.seiun.ui.notification.NotificationScreen
import io.github.akiomik.seiun.ui.registration.RegistrationScreen
import io.github.akiomik.seiun.ui.theme.SeiunTheme
import io.github.akiomik.seiun.ui.timeline.NewPostFab
import io.github.akiomik.seiun.ui.timeline.TimelineScreen
import io.github.akiomik.seiun.viewmodel.TimelineViewModel
import kotlinx.coroutines.launch

class SeiunActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val from = intent.getStringExtra("from")
        Log.d(SeiunApplication.TAG, "from = $from")

        setContent {
            App(from)
        }
    }
}

@Composable
private fun DrawerAvatar(profile: Profile?) {
    AsyncImage(
        model = profile?.avatar,
        contentDescription = null,
        modifier = Modifier
            .width(48.dp)
            .height(48.dp)
            .clip(CircleShape)
    )
}

@Composable
fun Drawer(state: DrawerState, content: @Composable () -> Unit) {
    val viewModel: TimelineViewModel = viewModel()
    val profile by viewModel.profile.observeAsState()
    val scope = rememberCoroutineScope()
    val items = listOf(Icons.Default.Favorite, Icons.Default.Face, Icons.Default.Email)
    val selectedItem = remember { mutableStateOf(items[0]) }

    ModalNavigationDrawer(
        drawerState = state,
        drawerContent = {
            ModalDrawerSheet {
//                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                ) {
                    DrawerAvatar(profile = profile)
                    Column {
                        Text(profile?.displayName.orEmpty())
                        Text(profile?.handle.orEmpty())
                    }
                }
                Divider()
//                items.forEach { item ->
//                    NavigationDrawerItem(
//                        icon = { Icon(item, contentDescription = null) },
//                        label = { Text(item.name) },
//                        selected = item == selectedItem.value,
//                        onClick = {
//                            scope.launch { state.close() }
//                            selectedItem.value = item
//                        },
//                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
//                    )
//                }
            }
        },
        content = content
    )
}

@Composable
private fun TopBarAvatar(profile: Profile?, drawerState: DrawerState) {
    val scope = rememberCoroutineScope()

    AsyncImage(
        model = profile?.avatar,
        contentDescription = null,
        modifier = Modifier
            .width(36.dp)
            .height(36.dp)
            .clip(CircleShape)
            .clickable { scope.launch { drawerState.open() } }
    )
}

@Composable
fun AppName(drawerState: DrawerState) {
    val scope = rememberCoroutineScope()

    Text(
        text = stringResource(R.string.app_name),
        modifier = Modifier.clickable { scope.launch { drawerState.open() } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(scrollBehavior: TopAppBarScrollBehavior, visible: Boolean, drawerState: DrawerState) {
    val viewModel: TimelineViewModel = viewModel()
    val profile by viewModel.profile.observeAsState()

    AnimatedVisibility(visible = visible) {
        CenterAlignedTopAppBar(
            title = { AppName(drawerState) },
            navigationIcon = { TopBarAvatar(profile, drawerState) },
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
fun BottomBar(
    navController: NavController,
    visible: Boolean,
    timelineListState: LazyListState,
    notificationListState: LazyListState
) {
    val items = listOf(Screen.Timeline, Screen.Notification)
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(visible = visible) {
        NavigationBar {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            items.forEach { screen ->
                NavigationBarItem(
                    icon = { Icon(screen.icon, contentDescription = null, tint = Color.White) },
                    label = { Text(stringResource(screen.resourceId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navController.navigate(screen.route) {
                            if (screen.route == "timeline" && screen.route == currentDestination?.route) {
                                coroutineScope.launch {
                                    timelineListState.animateScrollToItem(0)
                                }
                            }

                            if (screen.route == "notification" && screen.route == currentDestination?.route) {
                                coroutineScope.launch {
                                    notificationListState.animateScrollToItem(0)
                                }
                            }

                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun Navigation(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(from: String?) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val timelineListState = rememberLazyListState()
    val notificationListState = rememberLazyListState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var topBarState by rememberSaveable { (mutableStateOf(false)) }
    var bottomBarState by rememberSaveable { (mutableStateOf(false)) }
    var fabState by remember { (mutableStateOf<@Composable () -> Unit>({})) }

    val startDestination =
        if (from == "notification" && SeiunApplication.instance!!.isAtpServiceInitialized()) {
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

    SeiunTheme {
        Drawer(drawerState) {
            Scaffold(
                topBar = { TopBar(scrollBehavior, visible = topBarState, drawerState = drawerState) },
                bottomBar = {
                    BottomBar(
                        navController = navController,
                        visible = bottomBarState,
                        timelineListState = timelineListState,
                        notificationListState = notificationListState
                    )
                },
                floatingActionButton = fabState,
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                content = {
                    Navigation(
                        navController = navController,
                        modifier = Modifier.padding(it),
                        timelineListState = timelineListState,
                        notificationListState = notificationListState,
                        startDestination = startDestination
                    )
                }
            )
        }
    }
}
