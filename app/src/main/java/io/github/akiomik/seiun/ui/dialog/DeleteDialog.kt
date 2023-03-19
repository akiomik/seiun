package io.github.akiomik.seiun.ui.dialog

import android.util.Log
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import io.github.akiomik.seiun.viewmodels.PostViewModel

@Composable
fun DeleteDialog(feedViewPost: FeedViewPost, onDismissRequest: () -> Unit) {
    val viewModel: PostViewModel = viewModel()
    val context = LocalContext.current

    ConfirmDialog(
        body = {
            Text(stringResource(id = R.string.dialog_delete_confirmation))
        },
        action = {
            TextButton(onClick = {
                viewModel.deletePost(feedViewPost, onSuccess = {
                    onDismissRequest()
                }, onError = {
                        Log.d(SeiunApplication.TAG, it.toString())
                        onDismissRequest()
                        Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                    })
            }) {
                Text(stringResource(R.string.delete))
            }
        },
        onDismissRequest = onDismissRequest
    )
}
