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
import io.github.akiomik.seiun.model.app.bsky.actor.RefWithInfo
import io.github.akiomik.seiun.viewmodel.TimelineViewModel

@Composable
fun MuteDialog(actor: RefWithInfo, onDismissRequest: () -> Unit) {
    val viewModel: TimelineViewModel = viewModel()
    val context = LocalContext.current
    val mutedMessage = stringResource(id = R.string.timeline_muted)

    ConfirmDialog(body = {
        Text(stringResource(R.string.timeline_mute_confirmation, actor.handle))
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
