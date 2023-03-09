package io.github.akiomik.seiun.ui.timeline

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.model.FeedPost
import io.github.akiomik.seiun.model.FeedViewPost
import io.github.akiomik.seiun.ui.embed.EmbedPost
import io.github.akiomik.seiun.viewmodel.TimelineViewModel

@Composable
private fun PostButton(
    content: String,
    enabled: Boolean,
    feedViewPost: FeedViewPost?,
    onSuccess: () -> Unit
) {
    val viewModel: TimelineViewModel = viewModel()
    val context = LocalContext.current

    Button(onClick = {
        if (feedViewPost == null) {
            viewModel.createPost(content, onSuccess = onSuccess, onError = {
                Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
            })
        } else {
            viewModel.createReply(
                content = content,
                feedViewPost = feedViewPost,
                onSuccess = onSuccess,
                onError = {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                })
        }
    }, enabled = enabled) {
        Text(stringResource(id = R.string.timeline_new_post_post_button))
    }
}

@Composable
private fun PostContentField(content: String, onChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }

    TextField(
        value = content,
        onValueChange = onChange,
        label = { Text(stringResource(id = R.string.timeline_new_post_content)) },
        placeholder = { Text(text = stringResource(id = R.string.timeline_new_post_content_placeholder)) },
        maxLines = 8,
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .height(320.dp)
            .focusRequester(focusRequester),
        supportingText = {
            Text(
                text = "${content.length} / 256",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }
    )

    LaunchedEffect(Unit){
        focusRequester.requestFocus()
    }
}

@Composable
fun NewPostForm(feedViewPost: FeedViewPost?, onClose: () -> Unit) {
    var content by remember { mutableStateOf("") }
    var valid by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onClose) {
                Text(stringResource(id = R.string.timeline_new_post_cancel_button))
            }
            PostButton(content = content, enabled = valid, feedViewPost = feedViewPost) { onClose() }
        }

        Spacer(modifier = Modifier.size(8.dp))

        if (feedViewPost != null) {
            Box(modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()) {
                EmbedPost(viewPost = feedViewPost)
            }
        }

        PostContentField(content = content) {
            content = it
            valid = content.isNotEmpty() && content.length <= 256
        }
    }
}

@Composable
fun NewPostFormModal(feedViewPost: FeedViewPost? = null, onClose: () -> Unit = {}) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NewPostForm(feedViewPost = feedViewPost, onClose = onClose)
        }
    }
}

