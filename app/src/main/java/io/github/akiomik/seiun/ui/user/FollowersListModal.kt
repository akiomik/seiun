package io.github.akiomik.seiun.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.model.app.bsky.actor.RefWithInfo
import io.github.akiomik.seiun.ui.dialog.FullScreenDialog
import io.github.akiomik.seiun.viewmodels.FollowersViewModel

@Composable
private fun Avatar(user: RefWithInfo, onClicked: (String) -> Unit) {
    AsyncImage(
        model = user.avatar,
        contentDescription = null,
        modifier = Modifier
            .width(56.dp)
            .height(56.dp)
            .clip(CircleShape)
            .clickable { onClicked(user.did) }
    )
}

@Composable
private fun FollowersListItem(user: RefWithInfo, onProfileClick: (String) -> Unit) {
    ListItem(
        leadingContent = { Avatar(user = user, onClicked = onProfileClick) },
        headlineContent = { Text(text = user.displayName ?: "@${user.handle}") },
        supportingContent = {
            Text(
                text = "@${user.handle}",
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        },
        trailingContent = {
            // TODO
//            if (user.viewer?.following == null) {
//                Button(onClick = { /*TODO*/ }) {
//                    Text(stringResource(R.string.follow))
//                }
//            } else {
//                Button(onClick = { /*TODO*/ }) {
//                    Text(stringResource(R.string.unfollow))
//                }
//            }
        }
    )
}

@Composable
private fun FollowersListContent(
    followers: List<RefWithInfo>,
    viewModel: FollowersViewModel,
    onProfileClick: (String) -> Unit
) {
    val seenAllFollowers by viewModel.seenAllFollowers.collectAsState()

    LazyColumn {
        items(followers) {
            FollowersListItem(user = it, onProfileClick = onProfileClick)
            Divider(color = Color.Gray)
        }

        if (followers.isEmpty()) {
            item { Text(stringResource(R.string.followers_no_followers_yet)) }
        } else if (!seenAllFollowers) {
            item { FollowersLoadingIndicator(viewModel) }
        }
    }
}

// TODO: Implement as Screen
@Composable
fun FollowersListModal(
    viewModel: FollowersViewModel,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val followers by viewModel.followers.collectAsState()

    FullScreenDialog(onClose = onClose, actions = {}) {
        when (state) {
            is FollowersViewModel.State.Loading -> {
                CircularProgressIndicator()
            }
            is FollowersViewModel.State.Loaded -> {
                FollowersListContent(followers, viewModel, onProfileClick)
            }
            is FollowersViewModel.State.Error -> {
                Text((state as FollowersViewModel.State.Error).error.toString())
            }
        }
    }
}
