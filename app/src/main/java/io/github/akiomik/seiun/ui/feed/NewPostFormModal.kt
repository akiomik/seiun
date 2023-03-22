package io.github.akiomik.seiun.ui.feed

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import io.github.akiomik.seiun.ui.dialog.FullScreenDialog
import io.github.akiomik.seiun.ui.embed.EmbedPost
import io.github.akiomik.seiun.viewmodels.PostViewModel

@Composable
private fun PostButton(
    content: String,
    enabled: Boolean,
    feedViewPost: FeedViewPost?,
    imageUri: Uri?,
    onSuccess: () -> Unit
) {
    var isUploading by remember { mutableStateOf(false) }
    val viewModel: PostViewModel = viewModel()
    val context = LocalContext.current

    TextButton(onClick = {
        isUploading = true

        val image = imageUri?.let { uri ->
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            val bytes = viewModel.convertToUploadableImage(source)
            Pair(bytes, "image/jpeg")
        }

        val handleSuccess = {
            isUploading = false
            onSuccess()
        }

        val handleError = { error: Throwable ->
            isUploading = false
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
    }, enabled = enabled && !isUploading) {
        Text(stringResource(id = R.string.timeline_new_post_post_button))
    }
}

@Composable
private fun ImageSelectButton(onSelect: (Uri?) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { onSelect(it) }

    TextButton(onClick = {
        launcher.launch("image/*")
    }) {
        Icon(
            Icons.Sharp.Image,
            contentDescription = stringResource(R.string.timeline_new_post_image_upload_button)
        )
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
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val source = ImageDecoder.createSource(LocalContext.current.contentResolver, uri)

    if (bitmap == null) {
        bitmap = ImageDecoder.decodeBitmap(source)
    }

    bitmap?.let {
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
private fun NewPostForm(
    feedViewPost: FeedViewPost?,
    content: String,
    imageUri: Uri?,
    onContentChange: (String) -> Unit,
    onImageRemove: () -> Unit
) {
    Column(modifier = Modifier.padding(8.dp)) {
        if (feedViewPost != null) {
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                EmbedPost(viewPost = feedViewPost)
            }
        }

        PostContentField(content = content) { onContentChange(it) }

        imageUri?.let {
            ImagePreview(uri = it) { onImageRemove() }
        }
    }
}

@Composable
fun NewPostFormModal(feedViewPost: FeedViewPost? = null, onClose: () -> Unit) {
    val context = LocalContext.current
    val postedMessage = stringResource(R.string.feed_posted)
    var valid by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var content by remember { mutableStateOf("") }

    FullScreenDialog(
        onClose = onClose,
        actions = {
            ImageSelectButton {
                imageUri = it
                valid = true
            }

            Spacer(modifier = Modifier.width(24.dp))

            PostButton(
                content = content,
                enabled = valid,
                feedViewPost = feedViewPost,
                imageUri = imageUri
            ) {
                onClose()
                Toast.makeText(context, postedMessage, Toast.LENGTH_LONG).show()
            }
        }
    ) {
        NewPostForm(
            feedViewPost = feedViewPost,
            content = content,
            imageUri = imageUri,
            onContentChange = {
                content = it
                valid = content.isNotEmpty() && content.length <= 256 || imageUri != null
            },
            onImageRemove = {
                imageUri = null
                valid = content.isNotEmpty() && content.length <= 256
            }
        )
    }
}
