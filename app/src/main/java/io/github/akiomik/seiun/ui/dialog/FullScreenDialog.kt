package io.github.akiomik.seiun.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
private fun CloseButton(onClose: () -> Unit) {
    TextButton(onClick = onClose) {
        Icon(Icons.Sharp.Close, contentDescription = "Close")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenDialogTopBar(
    title: String?,
    onClose: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    CenterAlignedTopAppBar(
        title = { title?.let { Text(it) } },
        actions = actions,
        navigationIcon = { CloseButton(onClose = onClose) },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun FullScreenDialog(
    title: String? = null,
    onClose: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    FullScreenDialogTopBar(
                        title = title,
                        onClose = onClose,
                        actions = actions
                    )
                }
            ) {
                Box(modifier = Modifier.padding(it)) {
                    Spacer(modifier = Modifier.size(24.dp))
                    Box(modifier = Modifier.padding(8.dp)) {
                        content()
                    }
                }
            }
        }
    }
}
