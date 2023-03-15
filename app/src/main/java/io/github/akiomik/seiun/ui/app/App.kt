package io.github.akiomik.seiun.ui.app

import androidx.compose.runtime.Composable
import io.github.akiomik.seiun.ui.theme.SeiunTheme

@Composable
fun App(from: String?) {
    SeiunTheme {
        AppScaffold(from = from)
    }
}
