package io.github.akiomik.seiun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
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
fun Greeting(name: String) {
    Text(text = "Hello $name!")
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
                    Text("Loading")
                }
                is TimelineViewModel.State.Data -> {
                    LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
                        items(items = state.timeline.feed) { feedViewPost ->
                            Greeting(name = feedViewPost.post.record.text)
                        }
                    }
                }
            }
        }
    }
}