package io.github.akiomik.seiun.ui.app

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.ui.theme.SeiunTheme

@Composable
fun App(from: String?) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val atpService by SeiunApplication.instance!!.atpService.collectAsState()
    val drawerEnabled = atpService != null

    SeiunTheme {
        AppDrawer(drawerState, enabled = drawerEnabled) {
            AppScaffold(from = from, drawerState = drawerState)
        }
    }
}
