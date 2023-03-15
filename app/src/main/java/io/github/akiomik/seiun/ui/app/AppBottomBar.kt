package io.github.akiomik.seiun.ui.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import io.github.akiomik.seiun.Screen
import kotlinx.coroutines.launch

@Composable
fun AppBottomBar(
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
