package io.github.akiomik.seiun.ui.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.viewmodel.TimelineViewModel

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

@Composable
private fun AppName() {
    Text(text = stringResource(R.string.app_name))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(scrollBehavior: TopAppBarScrollBehavior, visible: Boolean) {
    val viewModel: TimelineViewModel = viewModel()
    val profile by viewModel.profile.observeAsState()

    AnimatedVisibility(visible = visible) {
        CenterAlignedTopAppBar(
            title = { AppName() },
            navigationIcon = { Avatar(profile) },
            scrollBehavior = scrollBehavior
        )
    }
}
