package io.github.akiomik.seiun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.akiomik.seiun.ui.login.LoginScreen
import io.github.akiomik.seiun.ui.notification.NotificationScreen
import io.github.akiomik.seiun.ui.registration.RegistrationScreen
import io.github.akiomik.seiun.ui.theme.Indigo800
import io.github.akiomik.seiun.ui.theme.SeiunTheme
import io.github.akiomik.seiun.ui.timeline.NewPostFab
import io.github.akiomik.seiun.ui.timeline.TimelineScreen

// アプリ起動時
class SeiunActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

// 画面下のバー
@Composable
fun BottomBar(navController: NavController, visible: MutableState<Boolean>) {
    // 表示するアイテム
    val items = listOf(Screen.Timeline, Screen.Notification)

    // 表示・非表示のアニメーション
    AnimatedVisibility(visible = visible.value) {
        // Navの表示設定
        BottomNavigation(backgroundColor = Indigo800, contentColor = Color.White) {
            // BackStackEntryの状態を取得
            val navBackStackEntry by navController.currentBackStackEntryAsState()

            // 現在の表示
            val currentDestination = navBackStackEntry?.destination
            items.forEach { screen ->
                // 各アイテムのセット
                BottomNavigationItem(
                    icon = { Icon(screen.icon, contentDescription = null, tint = Color.White) },
                    label = { Text(stringResource(screen.resourceId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            // 開始地点にポップアップすることで、項目を選択したときに、BackStackに大量に蓄積されるのを防ぐ
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            // 同じ項目を選択したときに同じアイテムをコピーすることを避ける
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            // 過去に選択した項目を再選択したときに、状態を復元する
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

//
@Composable
fun Navigation(navController: NavHostController, modifier: Modifier) {
    NavHost(navController = navController, startDestination = "login", modifier = Modifier) {
        composable("timeline") { TimelineScreen(/*...*/) }
        composable("notification") { NotificationScreen(/*...*/) }
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

// 起動時に呼び出されている
@Composable
fun App() {
    // NavigationコンポーネントのAPIで、構成するコンポーザブルのバックスタックと各画面の状態を追跡している
    val navController = rememberNavController()
    // BottomBarの表示状態を設定
    val bottomBarState = rememberSaveable { (mutableStateOf(false)) }
    // BackStackEntryの状態を取得
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // FloatingActionButtonの設定
    val fabState = remember { (mutableStateOf<@Composable () -> Unit>({})) }
    when (navBackStackEntry?.destination?.route) {
        // BottomBarを表示
        // ポストボタンを表示
        "timeline" -> {
            bottomBarState.value = true
            fabState.value = { NewPostFab() }
        }
        // BottomBarを表示
        // FABは非表示
        "notification" -> {
            bottomBarState.value = true
            fabState.value = {}
        }
        // BottomBarは非表示
        // FABは非表示
        else -> {
            bottomBarState.value = false
            fabState.value = {}
        }
    }

    // テーマに対して上記の設定を適用
    SeiunTheme {
        Scaffold(
            bottomBar = { BottomBar(navController = navController, visible = bottomBarState) },
            floatingActionButton = fabState.value
        ) {
            Navigation(navController = navController, modifier = Modifier.padding(it))
        }
    }
}