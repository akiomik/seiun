package io.github.akiomik.seiun.ui.timeline

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.viewmodel.TimelineViewModel

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

@Composable
private fun LoadingIndicator() {
    val viewModel: TimelineViewModel = viewModel()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }

    LaunchedEffect(key1 = true) {
        viewModel.loadMorePosts(onError = {
            Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
        })
    }
}

@Composable
fun NoPostsYetMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(id = R.string.timeline_no_posts_yet))
    }
}

@Composable
fun NoMorePostsMessage() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(stringResource(id = R.string.timeline_no_more_posts))
    }
}

@Composable
fun ErrorMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(id = R.string.timeline_error))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Timeline(listState: LazyListState) {
    val viewModel: TimelineViewModel = viewModel()
    val feedViewPosts = viewModel.feedViewPosts.observeAsState()
    val isRefreshing = viewModel.isRefreshing.observeAsState()
    val context = LocalContext.current
    val errored = viewModel.state.collectAsState().value
    val refreshState =
        rememberPullRefreshState(refreshing = isRefreshing.value ?: false, onRefresh = {
            viewModel.refreshPosts(onError = {
                Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
            })
        })

    Box(modifier = Modifier.pullRefresh(state = refreshState)) {
        LazyColumn(state = listState) {
            items(feedViewPosts.value.orEmpty()) { feedViewPost ->
                if (feedViewPost.post.viewer.muted != true) {
                    FeedPost(viewPost = feedViewPost)
                    Divider(color = Color.Gray)
                }
            }

            if (errored == TimelineViewModel.State.Error) {
                item { ErrorMessage() }
            } else if (viewModel.feedViewPosts.value?.size == 0) {
                item { NoPostsYetMessage() }
            } else if (viewModel.seenAllFeed.value == true) {
                item { NoMorePostsMessage() }
            } else {
                item { LoadingIndicator() }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing.value ?: false,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun TimelineScreen(listState: LazyListState) {
    val viewModel: TimelineViewModel = viewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (viewModel.state.collectAsState().value) {
            is TimelineViewModel.State.Loading -> LoadingText()
            is TimelineViewModel.State.Loaded -> Timeline(listState)
            is TimelineViewModel.State.Error -> Timeline(listState)
        }
    }
}
