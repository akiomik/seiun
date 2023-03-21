package io.github.akiomik.seiun.ui.app

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import io.github.akiomik.seiun.ui.theme.SeiunTheme
import io.github.akiomik.seiun.viewmodels.AppViewModel

@Composable
fun App(from: String?) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val navController = rememberNavController()
    val viewModel: AppViewModel = viewModel()
    val drawerEnabled by viewModel.showDrawer.collectAsState()

    SeiunTheme {
        AppDrawer(drawerState, enabled = drawerEnabled, onProfileClick = { profile ->
            navController.navigate("user/${profile.did}")
        }) {
            AppScaffold(from = from, drawerState = drawerState, navController = navController)
        }
    }
}
