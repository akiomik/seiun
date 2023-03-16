package io.github.akiomik.seiun.ui.app

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.viewmodels.AppViewModel
import kotlinx.coroutines.launch

@Composable
private fun Avatar(drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    val viewModel: AppViewModel = viewModel()
    val profile by viewModel.profile.collectAsState()

    AsyncImage(
        model = profile?.avatar,
        contentDescription = null,
        modifier = Modifier
            .width(36.dp)
            .height(36.dp)
            .clip(CircleShape)
            .clickable {
                Log.d(SeiunApplication.TAG, "clicked")
                scope.launch {
                    Log.d(SeiunApplication.TAG, "open")
                    drawerState.open()
                }
            }
    )
}

@Composable
private fun AppName() {
    Text(text = stringResource(R.string.app_name))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(scrollBehavior: TopAppBarScrollBehavior, visible: Boolean, drawerState: DrawerState) {
    val atpService by SeiunApplication.instance!!.atpService.collectAsState()

    AnimatedVisibility(visible = visible) {
        CenterAlignedTopAppBar(
            title = { AppName() },
            navigationIcon = {
                if (atpService != null) {
                    Avatar(drawerState)
                }
            },
            scrollBehavior = scrollBehavior
        )
    }
}
