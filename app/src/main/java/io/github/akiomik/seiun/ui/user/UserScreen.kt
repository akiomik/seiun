package io.github.akiomik.seiun.ui.user

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileDetail
import io.github.akiomik.seiun.ui.theme.Indigo800
import io.github.akiomik.seiun.viewmodels.AppViewModel
import io.github.akiomik.seiun.viewmodels.FollowsViewModel
import io.github.akiomik.seiun.viewmodels.UserFeedViewModel
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@Composable
private fun UserBanner(profile: ProfileDetail, height: Dp = 128.dp) {
    Box {
        // fallback
        Box(
            modifier = Modifier
                .background(color = Indigo800)
                .height(height)
                .fillMaxWidth()
        )

        AsyncImage(
            model = profile.banner,
            contentDescription = null,
            modifier = Modifier.height(height),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun Avatar(profile: ProfileDetail, modifier: Modifier = Modifier, size: Dp = 64.dp) {
    AsyncImage(
        model = profile.avatar,
        contentDescription = null,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    )
}

@Composable
private fun NameAndHandle(profile: ProfileDetail) {
    val handle = "@${profile.handle}"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = profile.displayName ?: handle,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = handle,
            color = Color.Gray,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun StatRow(
    profile: ProfileDetail,
    followsViewModel: FollowsViewModel,
    onProfileClick: (String) -> Unit
) {
    var showFollowsList by remember { mutableStateOf(false) }

    if (showFollowsList) {
        FollowsListModal(
            viewModel = followsViewModel,
            onProfileClick = onProfileClick,
            onClose = { showFollowsList = false }
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = profile.followsCount.toString(),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.follows),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable { showFollowsList = true }
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = profile.followersCount.toString(),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.followers),
                style = MaterialTheme.typography.labelMedium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = profile.postsCount.toString(),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.posts),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun FollowOrUnfollowButton(profile: ProfileDetail) {
    val context = LocalContext.current
    val viewModel: UserFeedViewModel = viewModel()

    if (profile.myState?.follow == null) {
        Button(onClick = {
            viewModel.follow(
                onSuccess = {},
                onError = { Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show() }
            )
        }) {
            Text(stringResource(R.string.follow))
        }
    } else {
        Button(onClick = {
            viewModel.unfollow(
                onSuccess = {},
                onError = { Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show() }
            )
        }) {
            Text(stringResource(R.string.unfollow))
        }
    }
}

@Composable
private fun Profile(
    profile: ProfileDetail,
    followsViewModel: FollowsViewModel,
    onProfileClick: (String) -> Unit
) {
    val viewModel: AppViewModel = viewModel()
    val viewer by viewModel.profile.collectAsState()
    var showEditProfile by remember { mutableStateOf(false) }

    if (showEditProfile) {
        ProfileEditModal(currentProfile = profile) {
            showEditProfile = false
        }
    }

    Column(
        modifier = Modifier.padding(top = 8.dp, end = 16.dp, bottom = 16.dp, start = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (profile.did == viewer?.did) {
                Button(onClick = {
                    showEditProfile = true
                }) {
                    Text(stringResource(R.string.edit))
                }
            } else {
                FollowOrUnfollowButton(profile = profile)
            }
        }

        NameAndHandle(profile = profile)

        Text(profile.description.orEmpty())

        StatRow(
            profile = profile,
            followsViewModel = followsViewModel,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
private fun UserModalContent(
    profile: ProfileDetail,
    followsViewModel: FollowsViewModel,
    onProfileClick: (String) -> Unit
) {
    val bannerHeight = 128.dp
    val avatarSize = 96.dp

    // TODO: Use ExitUntilCollapsed
    CollapsingToolbarScaffold(
        state = rememberCollapsingToolbarScaffoldState(),
        toolbar = {
            Column {
                Box(modifier = Modifier.zIndex(2f)) {
                    UserBanner(profile, height = bannerHeight)
                    Avatar(
                        profile = profile,
                        modifier = Modifier.offset(x = 16.dp, y = bannerHeight - (avatarSize / 2)),
                        size = avatarSize
                    )
                }
                Box(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.surfaceVariant)
                        .fillMaxWidth()
                ) {
                    Profile(profile, followsViewModel, onProfileClick)
                }
                Divider()
            }
        },
        scrollStrategy = ScrollStrategy.EnterAlways,
        enabled = true,
        modifier = Modifier.fillMaxSize()
    ) {
        UserFeed(profile, onProfileClick)
    }
}

@Composable
fun UserScreen(did: String, followsViewModel: FollowsViewModel, onProfileClick: (String) -> Unit) {
    val userFeedViewModel: UserFeedViewModel = viewModel()
    var profileRequested by remember { mutableStateOf(false) }
    val profile by userFeedViewModel.profile.collectAsState()

    if (!profileRequested) {
        userFeedViewModel.setProfileOf(
            did = did,
            onSuccess = { profileRequested = true },
            onError = { profileRequested = true }
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (profile == null) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            UserModalContent(
                profile = profile!!,
                followsViewModel = followsViewModel,
                onProfileClick = onProfileClick
            )
        }
    }
}
