package io.github.akiomik.seiun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

        setContent {
            App()
        }
    }
}

@Composable
private fun Avatar(profile: Profile?) {
    AsyncImage(
        model = profile?.avatar,
        contentDescription = null,
        modifier = Modifier
            .width(36.dp)
            .height(36.dp)
            .clip(CircleShape)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(scrollBehavior: TopAppBarScrollBehavior, visible: MutableState<Boolean>) {
    val viewModel: TimelineViewModel = viewModel()

    AnimatedVisibility(visible = visible.value) {
        CenterAlignedTopAppBar(
            title = { Text(stringResource(id = R.string.app_name)) },
            navigationIcon = { Avatar(viewModel.profile.value) },
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
fun BottomBar(
    navController: NavController,
    visible: MutableState<Boolean>,
    timelineListState: LazyListState,
    notificationListState: LazyListState
) {
    val items = listOf(Screen.Timeline, Screen.Notification)
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(visible = visible.value) {
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
    notificationListState: LazyListState
) {
    NavHost(navController = navController, startDestination = "login", modifier = modifier) {
        composable("timeline") { TimelineScreen(timelineListState) }
        composable("notification") { NotificationScreen(notificationListState) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val navController = rememberNavController()
    val topBarState = rememberSaveable { (mutableStateOf(false)) }
    val bottomBarState = rememberSaveable { (mutableStateOf(false)) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val fabState = remember { (mutableStateOf<@Composable () -> Unit>({})) }
    val timelineListState = rememberLazyListState()
    val notificationListState = rememberLazyListState()

    when (navBackStackEntry?.destination?.route) {
        "timeline" -> {
            topBarState.value = true
            bottomBarState.value = true
            fabState.value = { NewPostFab() }
        }
        "notification" -> {
            topBarState.value = true
            bottomBarState.value = true
            fabState.value = {}
        }
        else -> {
            topBarState.value = false
            bottomBarState.value = false
            fabState.value = {}
        }
    }


    SeiunTheme {
        Scaffold(
            topBar = { TopBar(scrollBehavior, visible = topBarState) },
            bottomBar = {
                BottomBar(
                    navController = navController,
                    visible = bottomBarState,
                    timelineListState = timelineListState,
                    notificationListState = notificationListState
                )
            },
            floatingActionButton = fabState.value,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            content = {
                Navigation(
                    navController = navController,
                    modifier = Modifier.padding(it),
                    timelineListState = timelineListState,
                    notificationListState = notificationListState
                )
            }
        )
    }
}