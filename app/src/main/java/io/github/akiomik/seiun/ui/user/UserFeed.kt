package io.github.akiomik.seiun.ui.user

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.ui.timeline.ErrorMessage
import io.github.akiomik.seiun.ui.timeline.FeedPost
import io.github.akiomik.seiun.ui.timeline.LoadingIndicator
import io.github.akiomik.seiun.ui.timeline.NoMorePostsMessage
import io.github.akiomik.seiun.ui.timeline.NoPostsYetMessage
import io.github.akiomik.seiun.viewmodels.UserFeedViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserFeed(profile: Profile) {
    val context = LocalContext.current
    val viewModel: UserFeedViewModel = viewModel()
    val feedViewPosts by viewModel.feedViewPosts.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val seenAllFeed by viewModel.seenAllFeed.collectAsState()
    val errored by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val refreshState =
        rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
            viewModel.refreshPosts(onError = {
                Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
            })
        })

    Box(modifier = Modifier.pullRefresh(state = refreshState).padding(top = 8.dp)) {
        LazyColumn(state = listState) {
            items(feedViewPosts) { feedViewPost ->
                if (feedViewPost.post.viewer.muted != true) {
                    FeedPost(viewPost = feedViewPost)
                    Divider(color = Color.Gray)
                }
            }

            if (errored == UserFeedViewModel.State.Error) {
                item { ErrorMessage() }
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
