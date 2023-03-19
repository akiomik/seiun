package io.github.akiomik.seiun.ui.user

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.ui.feed.FeedPost
import io.github.akiomik.seiun.ui.feed.LoadingErrorMessage
import io.github.akiomik.seiun.ui.feed.NoMorePostsMessage
import io.github.akiomik.seiun.ui.feed.NoPostsYetMessage
import io.github.akiomik.seiun.viewmodels.UserFeedViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserFeedContent() {
    val context = LocalContext.current
    val viewModel: UserFeedViewModel = viewModel()
    val feedViewPosts by viewModel.feedViewPosts.collectAsState()
    val seenAllFeed by viewModel.seenAllFeed.collectAsState()
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val listState = rememberLazyListState()
    val refreshState =
        rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
            viewModel.refreshPosts(
                onSuccess = {},
                onError = { Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show() }
            )
        })

    Box(modifier = Modifier.pullRefresh(state = refreshState)) {
        LazyColumn(state = listState) {
            items(feedViewPosts) { feedViewPost ->
                if (feedViewPost.post.viewer.muted != true) {
                    FeedPost(viewPost = feedViewPost)
                    Divider(color = Color.Gray)
                }
            }

            if (state == UserFeedViewModel.State.Error) {
                item { LoadingErrorMessage() }
            } else if (feedViewPosts.isEmpty()) {
                item { NoPostsYetMessage() }
            } else if (seenAllFeed) {
                item { NoMorePostsMessage() }
            } else {
                item { LoadingIndicator() }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun UserFeed(profile: Profile) {
    val viewModel: UserFeedViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    if (state == UserFeedViewModel.State.ProfileLoaded) {
        viewModel.setFeed(
            profile = profile,
            onSuccess = {},
            onError = { Log.d(SeiunApplication.TAG, it.toString()) }
        )
    }

    if (state == UserFeedViewModel.State.FeedLoaded) {
        UserFeedContent()
    } else {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
