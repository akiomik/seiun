package io.github.akiomik.seiun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.sharp.ChatBubbleOutline
import androidx.compose.material.icons.sharp.FavoriteBorder
import androidx.compose.material.icons.sharp.SyncAlt
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.akiomik.seiun.model.FeedViewPost
import io.github.akiomik.seiun.ui.theme.SeiunTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApp()
        }
    }
}

@Composable
fun RetweetText(viewPost: FeedViewPost) {
    Box(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "Reposted by ${viewPost.reason?.by?.displayName}",
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun ReplyText(viewPost: FeedViewPost) {
    Box(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "Replying to ${viewPost.reply?.parent?.author?.displayName}",
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun Avatar(viewPost: FeedViewPost) {
    AsyncImage(
        model = viewPost.post.author.avatar,
        contentDescription = null,
        modifier = Modifier
            .width(50.dp)
            .height(50.dp)
            .clip(CircleShape)
    )
}

@Composable
fun DisplayName(viewPost: FeedViewPost) {
    Text(
        text = "${viewPost.post.author.displayName}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun Handle(viewPost: FeedViewPost) {
    Text(
        text = "@${viewPost.post.author.handle}",
        style = MaterialTheme.typography.labelMedium,
        color = Color.Gray
    )
}

@Composable
fun NameRow(viewPost: FeedViewPost) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DisplayName(viewPost = viewPost)
        Handle(viewPost = viewPost)
    }
}

@Composable
fun ReplyIndicator(viewPost: FeedViewPost) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = rememberVectorPainter(Icons.Sharp.ChatBubbleOutline),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Text(text = viewPost.post.replyCount.toString(), color = Color.Gray)
    }
}

@Composable
fun RepostIndicator(viewPost: FeedViewPost) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = rememberVectorPainter(Icons.Sharp.SyncAlt),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Text(text = viewPost.post.repostCount.toString(), color = Color.Gray)
    }
}

@Composable
fun UpvoteIndicator(viewPost: FeedViewPost) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = rememberVectorPainter(Icons.Sharp.FavoriteBorder),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Text(text = viewPost.post.upvoteCount.toString(), color = Color.Gray)
    }
}

@Composable
fun FeedPostContent(viewPost: FeedViewPost) {
    Column(modifier = Modifier.padding(start = 8.dp)) {
        NameRow(viewPost = viewPost)
        Text(text = viewPost.post.record.text)
        Row(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ReplyIndicator(viewPost = viewPost)
            RepostIndicator(viewPost = viewPost)
            UpvoteIndicator(viewPost = viewPost)
        }
        Text(text = viewPost.post.record.createdAt)
    }
}

@Composable
fun FeedPost(viewPost: FeedViewPost) {
    Column(modifier = Modifier.padding(10.dp)) {
        if (viewPost.reason?.type == "app.bsky.feed.feedViewPost#reasonRepost")  {
            RetweetText(viewPost = viewPost)
        }
        else if (viewPost.reply != null) {
            ReplyText(viewPost = viewPost)
        }

        Row {
            Avatar(viewPost = viewPost)
            FeedPostContent(viewPost = viewPost)
        }
    }
}

@Composable
fun LoadingText() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Loading")
            CircularProgressIndicator()
        }
    }
}
@Composable
fun LoadingIndicator() {
    val viewModel: TimelineViewModel = viewModel()

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }

    LaunchedEffect(key1 = true) {
        viewModel.loadMorePosts()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Timeline() {
    val viewModel: TimelineViewModel = viewModel()
    val listState = rememberLazyListState()
    val feedViewPosts = viewModel.feedViewPosts.observeAsState()
    val isRefreshing = viewModel.isRefreshing.observeAsState()
    val refreshState = rememberPullRefreshState(refreshing = isRefreshing.value ?: false, onRefresh = {
        viewModel.refreshPosts()
    })

    Box(modifier = Modifier.pullRefresh(state = refreshState)) {
        LazyColumn(state = listState) {
            items(feedViewPosts.value.orEmpty()) { feedViewPost ->
                FeedPost(viewPost = feedViewPost)
                Divider(color = Color.Gray)
            }
            item { LoadingIndicator() }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing.value ?: false,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun MyApp() {
    val viewModel: TimelineViewModel = viewModel()

    SeiunTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (viewModel.state.collectAsState().value) {
                is TimelineViewModel.State.Loading -> LoadingText()
                is TimelineViewModel.State.Loaded -> Timeline()
            }
        }
    }
}