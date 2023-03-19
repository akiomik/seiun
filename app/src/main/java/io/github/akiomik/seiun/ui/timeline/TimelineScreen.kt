package io.github.akiomik.seiun.ui.timeline

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.ui.feed.FeedPost
import io.github.akiomik.seiun.ui.feed.LoadingErrorMessage
import io.github.akiomik.seiun.ui.feed.NoMorePostsMessage
import io.github.akiomik.seiun.ui.feed.NoPostsYetMessage
import io.github.akiomik.seiun.viewmodels.TimelineViewModel

@Composable
private fun LoadingText() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.loading))
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Timeline(listState: LazyListState) {
    val context = LocalContext.current
    val viewModel: TimelineViewModel = viewModel()
    val feedViewPosts by viewModel.feedPosts.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errored by viewModel.state.collectAsState()
    val seenAllFeed by viewModel.seenAllFeed.collectAsState()
    val refreshState =
        rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
            viewModel.refreshPosts(onError = {
                Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
            })
        })

    Box(modifier = Modifier.pullRefresh(state = refreshState)) {
        LazyColumn(state = listState) {
            items(feedViewPosts) { feedViewPost ->
                if (feedViewPost.post.viewer.muted != true) {
                    FeedPost(viewPost = feedViewPost)
                    Divider(color = Color.Gray)
                }
            }

            if (errored == TimelineViewModel.State.Error) {
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
fun TimelineScreen(listState: LazyListState) {
    val viewModel: TimelineViewModel = viewModel()
    val timelineState by viewModel.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (timelineState) {
            is TimelineViewModel.State.Loading -> LoadingText()
            is TimelineViewModel.State.Loaded -> Timeline(listState)
            is TimelineViewModel.State.Error -> Timeline(listState)
        }
    }
}
