package io.github.akiomik.seiun.ui.user

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileDetail
import io.github.akiomik.seiun.ui.components.SingleLineTextField
import io.github.akiomik.seiun.ui.dialog.FullScreenDialog
import io.github.akiomik.seiun.viewmodels.UserFeedViewModel

@Composable
fun ProfileEditModal(currentProfile: ProfileDetail, onDismissRequest: () -> Unit) {
    val viewModel: UserFeedViewModel = viewModel()
    val context = LocalContext.current
    val updatedMessage = stringResource(id = R.string.dialog_updated)
//    val imageUploadingMessage = stringResource(id = R.string.dialog_uploading_image)
    var displayName by remember { mutableStateOf(currentProfile.displayName.orEmpty()) }
    var description by remember { mutableStateOf(currentProfile.description.orEmpty()) }
//    var avatarUri by remember { mutableStateOf(currentProfile.avatar) }
//    var bannerUri by remember { mutableStateOf(currentProfile.banner) }

    FullScreenDialog(
        title = stringResource(R.string.edit),
        onClose = onDismissRequest,
        actions = {
            TextButton(onClick = {
                // TODO
                val avatar = null
                val banner = null
//            val avatar =
//                if (avatarUri.isNullOrBlank()) {
//                    null
//                } else if (avatarUri != currentProfile.avatar) {
//                    // TODO: Get image cid and mimeType
//                    Image("foo", "bar")
//                } else {
//                    Toast.makeText(context, imageUploadingMessage, Toast.LENGTH_LONG).show()
//
//                    // TODO: Upload image
//                    Image("foo", "bar")
//                }
//
//            val banner =
//                if (bannerUri.isNullOrBlank()) {
//                    null
//                } else if (bannerUri != currentProfile.banner) {
//                    // TODO: Get image cid and mimeType
//                    Image("foo", "bar")
//                } else {
//                    Toast.makeText(context, imageUploadingMessage, Toast.LENGTH_LONG).show()
//
//                    // TODO: Upload image
//                    Image("foo", "bar")
//                }

                val profile = Profile(
                    displayName = displayName,
                    description = description,
                    avatar = avatar,
                    banner = banner
                )

                viewModel.updateProfile(
                    profile,
                    onSuccess = {
                        onDismissRequest()
                        Toast.makeText(context, updatedMessage, Toast.LENGTH_LONG).show()
                    },
                    onError = {
                        Log.d(SeiunApplication.TAG, it.toString())
                        onDismissRequest()
                        Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                    }
                )
            }) {
                Text(stringResource(R.string.update))
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SingleLineTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text(stringResource(id = R.string.display_name)) },
                placeholder = { Text(text = stringResource(id = R.string.dialog_display_name_placeholder)) },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(id = R.string.description)) },
                placeholder = { Text(text = stringResource(id = R.string.optional)) },
                modifier = Modifier
                    .height(196.dp)
                    .fillMaxWidth()
            )
        }
    }
}
