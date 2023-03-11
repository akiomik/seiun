package io.github.akiomik.seiun.ui.timeline

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import io.github.akiomik.seiun.ui.embed.EmbedPost
import io.github.akiomik.seiun.viewmodel.TimelineViewModel

@Composable
private fun PostButton(
    content: String,
    enabled: Boolean,
    feedViewPost: FeedViewPost?,
    imageUri: Uri?,
    onSuccess: () -> Unit
) {
    val isUploading = remember { mutableStateOf(false) }
    val viewModel: TimelineViewModel = viewModel()
    val context = LocalContext.current

    Button(onClick = {
        isUploading.value = true

        val image = imageUri?.let { uri ->
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            val bytes = viewModel.convertToUploadableImage(source)
            Pair(bytes, "image/jpeg")
        }

        val handleSuccess = {
            isUploading.value = false
            onSuccess()
        }

        val handleError = { error: Throwable ->
            isUploading.value = false
            Log.d(SeiunApplication.TAG, error.toString())
            Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show()
        }

        if (feedViewPost == null) {
            viewModel.createPost(
                content,
                image?.first,
                image?.second,
                onSuccess = handleSuccess,
                onError = handleError
            )
        } else {
            viewModel.createReply(
                content,
                feedViewPost,
                image?.first,
                image?.second,
                onSuccess = handleSuccess,
                onError = handleError
            )
        }
    }, enabled = enabled && !isUploading.value) {
        Text(stringResource(id = R.string.timeline_new_post_post_button))
    }
}

@Composable
fun ImageSelectButton(onSelect: (Uri?) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { onSelect(it) }

    Button(onClick = {
        launcher.launch("image/*")
    }) {
        Text(stringResource(id = R.string.timeline_new_post_image_upload_button))
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
                textAlign = TextAlign.End
            )
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun ImagePreview(uri: Uri, onDelete: () -> Unit) {
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    val source = ImageDecoder.createSource(LocalContext.current.contentResolver, uri)

    if (bitmap.value == null) {
        bitmap.value = ImageDecoder.decodeBitmap(source)
    }

    bitmap.value?.let {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onDelete) {
                Text(stringResource(id = R.string.timeline_new_post_delete_image))
            }
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(400.dp)
            )
        }
    }
}

@Composable
fun NewPostForm(feedViewPost: FeedViewPost?, onClose: () -> Unit) {
    var content by remember { mutableStateOf("") }
    var valid by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    Column(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onClose) {
                Text(stringResource(id = R.string.timeline_new_post_cancel_button))
            }
            ImageSelectButton {
                imageUri = it
                valid = true
            }
            PostButton(
                content = content,
                enabled = valid,
                feedViewPost = feedViewPost,
                imageUri = imageUri
            ) { onClose() }
        }

        Spacer(modifier = Modifier.size(8.dp))

        if (feedViewPost != null) {
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                EmbedPost(viewPost = feedViewPost)
            }
        }

        PostContentField(content = content) {
            content = it
            valid = content.isNotEmpty() && content.length <= 256 || imageUri != null
        }

        imageUri?.let {
            ImagePreview(uri = it) {
                imageUri = null
                valid = content.isNotEmpty() && content.length <= 256
            }
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
