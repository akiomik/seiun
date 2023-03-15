package io.github.akiomik.seiun.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.viewmodel.AppViewModel

@Composable
private fun Profile() {
    val viewModel: AppViewModel = viewModel()
    val profile by viewModel.profile.observeAsState()

    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Avatar(profile)
        NameAndHandle(profile)
    }
}

@Composable
private fun Avatar(profile: Profile?) {
    AsyncImage(
        model = profile?.avatar,
        contentDescription = null,
        modifier = Modifier
            .width(48.dp)
            .height(48.dp)
            .clip(CircleShape)
    )
}

@Composable
private fun NameAndHandle(profile: Profile?) {
    Column {
        Text(
            text = profile?.displayName.orEmpty(),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = profile?.handle?.let { "@$it" }.orEmpty(),
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun AppDrawer(state: DrawerState, enabled: Boolean, content: @Composable () -> Unit) {
    ModalNavigationDrawer(
        drawerState = state,
        gesturesEnabled = enabled,
        drawerContent = {
            ModalDrawerSheet {
                if (enabled) {
                    Profile()
                    Divider()
                }
            }
        },
        content = content
    )
}
