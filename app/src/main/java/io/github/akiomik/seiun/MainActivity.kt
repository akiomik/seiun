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
import androidx.compose.material.icons.sharp.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.akiomik.seiun.model.FeedViewPost
import io.github.akiomik.seiun.ui.theme.SeiunTheme
import io.github.akiomik.seiun.viewmodel.TimelineViewModel


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
    TextButton(
        modifier = Modifier.width(64.dp),
        onClick = { /*TODO*/ }
    ) {
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
}

@Composable
fun RepostIndicator(viewPost: FeedViewPost) {
    val viewModel: TimelineViewModel = viewModel()
    var reposted by remember { mutableStateOf(viewPost.post.viewer.repost != null) }
    var count by remember { mutableStateOf(viewPost.post.repostCount) }
    val color: Color = if (reposted) {
        colorResource(R.color.green_700)
    } else {
        Color.Gray
    }

    TextButton(
        modifier = Modifier.width(64.dp),
        onClick = {
            if (reposted) {
                viewModel.cancelRepost(viewPost.post, onComplete = {
                    reposted = false
                    count -= 1
                })
            } else {
                viewModel.repost(viewPost.post, onComplete = {
                    reposted = true
                    count += 1
                })
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Sharp.SyncAlt),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(text = count.toString(), color = color)
        }
    }
}

@Composable
fun UpvoteIndicator(viewPost: FeedViewPost) {
    val viewModel: TimelineViewModel = viewModel()
    var upvoted by remember { mutableStateOf(viewPost.post.viewer.upvote != null) }
    var count by remember { mutableStateOf(viewPost.post.upvoteCount) }
    val color = if (upvoted) {
        colorResource(R.color.red_700)
    } else {
        Color.Gray
    }
    val icon = if (upvoted) {
        Icons.Sharp.Favorite
    } else {
        Icons.Sharp.FavoriteBorder
    }

    TextButton(
        modifier = Modifier.width(64.dp),
        onClick = {
            if (upvoted) {
                viewModel.cancelVote(feedPost = viewPost.post) {
                    upvoted = false
                    count -= 1
                }
            } else {
                viewModel.upvote(feedPost = viewPost.post) {
                    upvoted = true
                    count += 1
                }
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = rememberVectorPainter(icon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(text = count.toString(), color = color)
        }
    }
}

@Composable
fun FeedPostContent(viewPost: FeedViewPost) {
    Column(modifier = Modifier.padding(start = 8.dp)) {
        NameRow(viewPost = viewPost)
        Text(text = viewPost.post.record.text)
        Row(
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReplyIndicator(viewPost = viewPost)
            RepostIndicator(viewPost = viewPost)
            UpvoteIndicator(viewPost = viewPost)
        }
        Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = viewPost.post.record.createdAt,
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun FeedPost(viewPost: FeedViewPost) {
    Column(modifier = Modifier.padding(10.dp)) {
        if (viewPost.reason?.type == "app.bsky.feed.feedViewPost#reasonRepost") {
            RetweetText(viewPost = viewPost)
        } else if (viewPost.reply != null) {
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
fun LoadingIndicator() {
    val viewModel: TimelineViewModel = viewModel()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp), contentAlignment = Alignment.Center
    ) {
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
    val refreshState =
        rememberPullRefreshState(refreshing = isRefreshing.value ?: false, onRefresh = {
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
        NewPostButton()
    }
}

@Composable
fun PostButton(content: String, enabled: Boolean, onComplete: () -> Unit) {
    val viewModel: TimelineViewModel = viewModel()

    Button(onClick = {
        // TODO: Check post completion
        viewModel.createPost(content)
        onComplete()
    }, enabled = enabled) {
        Text("Post")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostContentField(content: String, onChange: (String) -> Unit) {
    TextField(
        value = content,
        onValueChange = onChange,
        label = { Text("Content") },
        placeholder = { Text(text = "What's up?") },
        maxLines = 8,
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .height(320.dp),
        supportingText = {
            Text(
                text = "${content.length} / 256",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewPostForm(onClose: () -> Unit) {
    var content by remember { mutableStateOf("") }
    var valid by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onClose) {
                        Text("Cancel")
                    }
                    PostButton(content = content, enabled = valid) { onClose() }
                }

                Spacer(modifier = Modifier.size(8.dp))

                PostContentField(content = content) {
                    content = it
                    valid = content.isNotEmpty() && content.length <= 256
                }
            }
        }
    }
}

@Composable
fun NewPostButton() {
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