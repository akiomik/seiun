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
import io.github.akiomik.seiun.model.app.bsky.actor.WithInfo
import io.github.akiomik.seiun.ui.dialog.FullScreenDialog
import io.github.akiomik.seiun.viewmodels.FollowsViewModel

@Composable
private fun Avatar(user: WithInfo, onClicked: (String) -> Unit) {
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
private fun FollowsListItem(user: WithInfo, onProfileClick: (String) -> Unit) {
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
private fun FollowsListContent(
    follows: List<WithInfo>,
    viewModel: FollowsViewModel,
    onProfileClick: (String) -> Unit
) {
    val seenAllFollows by viewModel.seenAllFollows.collectAsState()

    LazyColumn {
        items(follows) {
            FollowsListItem(user = it, onProfileClick = onProfileClick)
            Divider(color = Color.Gray)
        }

        if (follows.isEmpty()) {
            item { Text(stringResource(R.string.follows_no_follows_yet)) }
        } else if (!seenAllFollows) {
            item { FollowsLoadingIndicator(viewModel) }
        }
    }
}

// TODO: Implement as Screen
@Composable
fun FollowsListModal(
    viewModel: FollowsViewModel,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val follows by viewModel.follows.collectAsState()

    FullScreenDialog(onClose = onClose, actions = {}) {
        when (state) {
            is FollowsViewModel.State.Loading -> {
                CircularProgressIndicator()
            }
            is FollowsViewModel.State.Loaded -> {
                FollowsListContent(follows, viewModel, onProfileClick)
            }
            is FollowsViewModel.State.Error -> {
                Text((state as FollowsViewModel.State.Error).error.toString())
            }
        }
    }
}
