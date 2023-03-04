package io.github.akiomik.seiun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import io.github.akiomik.seiun.model.FeedPost
import io.github.akiomik.seiun.ui.theme.SeiunTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: TimelineViewModel by lazy {
            ViewModelProvider(this).get(TimelineViewModel::class.java)
        }

        setContent {
            MyApp(viewModel)
        }
    }
}

@Composable
fun FeedPost(post: FeedPost) {
    Row(modifier = Modifier.padding(10.dp)) {
        AsyncImage(
            model = post.author.avatar,
            contentDescription = null,
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
                .clip(CircleShape)
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${post.author.displayName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${post.author.handle}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
            Text(text = post.record.text)
        }
    }
}

@Composable
fun LoadingText() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Loading")
    }
}

@Composable
fun MyApp(
    viewModel: TimelineViewModel
) {
    SeiunTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val state = viewModel.state.collectAsState().value) {
                TimelineViewModel.State.Loading -> {
                    LoadingText()
                }
                is TimelineViewModel.State.Data -> {
                    LazyColumn {
                        items(state.timeline.feed) { feedViewPost ->
                            FeedPost(post = feedViewPost.post)
                            Divider(color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}