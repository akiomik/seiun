package io.github.akiomik.seiun.ui.timeline

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.sharp.ChatBubbleOutline
import androidx.compose.material.icons.sharp.Favorite
import androidx.compose.material.icons.sharp.FavoriteBorder
import androidx.compose.material.icons.sharp.SyncAlt
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import io.github.akiomik.seiun.model.FeedViewPost
import io.github.akiomik.seiun.ui.theme.Green700
import io.github.akiomik.seiun.ui.theme.Red700
import io.github.akiomik.seiun.ui.theme.SeiunTheme
import io.github.akiomik.seiun.viewmodel.TimelineViewModel

@Composable
private fun RetweetText(viewPost: FeedViewPost) {
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
private fun ReplyText(viewPost: FeedViewPost) {
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
private fun Avatar(viewPost: FeedViewPost) {
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
private fun DisplayName(viewPost: FeedViewPost) {
    Text(
        text = "${viewPost.post.author.displayName}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Handle(viewPost: FeedViewPost) {
    Text(
        text = "@${viewPost.post.author.handle}",
        style = MaterialTheme.typography.labelMedium,
        color = Color.Gray
    )
}

@Composable
private fun NameRow(viewPost: FeedViewPost) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DisplayName(viewPost = viewPost)
        Handle(viewPost = viewPost)
    }
}

@Composable
private fun ReplyIndicator(viewPost: FeedViewPost) {
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
private fun RepostIndicator(viewPost: FeedViewPost) {
    val viewModel: TimelineViewModel = viewModel()
    val reposted = viewPost.post.viewer.repost != null
    val color: Color = if (reposted) {
        Green700
    } else {
        Color.Gray
    }
    val context = LocalContext.current

    TextButton(
        modifier = Modifier.width(64.dp),
        onClick = {
            if (reposted) {
                viewModel.cancelRepost(viewPost.post, onError = {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                })
            } else {
                viewModel.repost(viewPost.post, onError = {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
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
            Text(text = viewPost.post.repostCount.toString(), color = color)
        }
    }
}

@Composable
private fun UpvoteIndicator(viewPost: FeedViewPost) {
    val viewModel: TimelineViewModel = viewModel()
    val upvoted = viewPost.post.viewer.upvote != null
    val color = if (upvoted) {
        Red700
    } else {
        Color.Gray
    }
    val icon = if (upvoted) {
        Icons.Sharp.Favorite
    } else {
        Icons.Sharp.FavoriteBorder
    }
    val context = LocalContext.current

    TextButton(
        modifier = Modifier.width(64.dp),
        onClick = {
            if (upvoted) {
                viewModel.cancelVote(feedPost = viewPost.post, onError = {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                })
            } else {
                viewModel.upvote(feedPost = viewPost.post, onError = {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                })
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
            Text(text = viewPost.post.upvoteCount.toString(), color = color)
        }
    }
}

@Composable
private fun FeedPostContent(viewPost: FeedViewPost) {
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
private fun FeedPost(viewPost: FeedViewPost) {
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
private fun PostButton(content: String, enabled: Boolean, onSuccess: () -> Unit) {
    val viewModel: TimelineViewModel = viewModel()
    val context = LocalContext.current

    Button(onClick = {
        viewModel.createPost(content, onSuccess = onSuccess, onError = {
            Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
        })
    }, enabled = enabled) {
        Text("Post")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostContentField(content: String, onChange: (String) -> Unit) {
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NewPostForm(onClose: () -> Unit) {
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
        when (viewModel.state.collectAsState().value) {
            is TimelineViewModel.State.Loading -> LoadingText()
            is TimelineViewModel.State.Loaded -> Timeline()
        }
    }
}