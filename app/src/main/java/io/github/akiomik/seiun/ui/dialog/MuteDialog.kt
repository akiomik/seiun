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
import io.github.akiomik.seiun.model.app.bsky.actor.WithInfo
import io.github.akiomik.seiun.viewmodels.PostViewModel

@Composable
fun MuteDialog(actor: WithInfo, onDismissRequest: () -> Unit) {
    val viewModel: PostViewModel = viewModel()
    val context = LocalContext.current
    val mutedMessage = stringResource(id = R.string.dialog_muted)

    ConfirmDialog(body = {
        Text(stringResource(R.string.dialog_mute_confirmation, actor.handle))
    }, action = {
            TextButton(onClick = {
                viewModel.mute(actor.did, onSuccess = {
                    onDismissRequest()
                    Toast.makeText(context, mutedMessage, Toast.LENGTH_LONG).show()
                }, onError = {
                        Log.d(SeiunApplication.TAG, it.toString())
                        onDismissRequest()
                        Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                    })
            }) {
                Text(stringResource(R.string.mute))
            }
        }, onDismissRequest = onDismissRequest)
}
