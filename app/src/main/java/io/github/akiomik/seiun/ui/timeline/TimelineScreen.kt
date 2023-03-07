package io.github.akiomik.seiun.ui.timeline

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.viewmodel.TimelineViewModel

@Composable
private fun LoadingText() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Loading")
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
            .padding(8.dp), contentAlignment = Alignment.Center
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
        Text("No posts yet")
    }
}

@Composable
fun NoMorePostsMessage() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text("No more posts")
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Timeline() {
    val viewModel: TimelineViewModel = viewModel()
    val listState = rememberLazyListState()
    val feedViewPosts = viewModel.feedViewPosts.observeAsState()
    val isRefreshing = viewModel.isRefreshing.observeAsState()
    val context = LocalContext.current
    val refreshState =
        rememberPullRefreshState(refreshing = isRefreshing.value ?: false, onRefresh = {
            viewModel.refreshPosts(onError = {
                Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
            })
        })

    Box(modifier = Modifier.pullRefresh(state = refreshState)) {
        LazyColumn(state = listState) {
            items(feedViewPosts.value.orEmpty()) { feedViewPost ->
                FeedPost(viewPost = feedViewPost)
                Divider(color = Color.Gray)
            }

            if (viewModel.feedViewPosts.value?.size == 0) {
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
        NewPostButton()
    }
}

@Composable
private fun NewPostButton() {
    var showPostForm by remember { mutableStateOf(false) }
    if (showPostForm) {
        NewPostForm { showPostForm = false }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                modifier = Modifier
                    .padding(16.dp)
                    .align(alignment = Alignment.BottomEnd),
                onClick = {
                    showPostForm = true
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create new post")
            }
        }
    }
}

@Composable
fun TimelineScreen() {
    val viewModel: TimelineViewModel = viewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Log.d("Seiun", viewModel.state.collectAsState().value.toString())
        when (viewModel.state.collectAsState().value) {
            is TimelineViewModel.State.Loading -> LoadingText()
            is TimelineViewModel.State.Loaded -> {
                Timeline()
            }
        }
    }
}