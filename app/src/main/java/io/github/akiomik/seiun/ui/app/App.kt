package io.github.akiomik.seiun.ui.app

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.ui.theme.SeiunTheme
import io.github.akiomik.seiun.viewmodels.AppViewModel

@Composable
fun App(from: String?) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val viewModel: AppViewModel = viewModel()
    val profile by viewModel.profile.collectAsState()
    val atpService by SeiunApplication.instance!!.atpService.collectAsState()
    val drawerEnabled = atpService != null && profile != null

    SeiunTheme {
        AppDrawer(drawerState, enabled = drawerEnabled) {
            AppScaffold(from = from, drawerState = drawerState)
        }
    }
}
