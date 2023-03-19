package io.github.akiomik.seiun.ui.dialog

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import io.github.akiomik.seiun.ui.embed.EmbedPost
import io.github.akiomik.seiun.viewmodels.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeDropdownMenu(onChange: (String) -> Unit) {
    val typeTextSpam = stringResource(id = R.string.spam)
    val typeTextOthers = stringResource(id = R.string.others)

    var typeExpanded by remember { mutableStateOf(false) }
    var typeText by remember { mutableStateOf(typeTextSpam) }

    ExposedDropdownMenuBox(
        expanded = typeExpanded,
        onExpandedChange = { typeExpanded = !typeExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            readOnly = true,
            value = typeText,
            label = { Text(stringResource(R.string.dialog_report_type)) },
            modifier = Modifier.menuAnchor(),
            onValueChange = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = typeExpanded,
            onDismissRequest = { typeExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.spam)) },
                onClick = {
                    typeText = typeTextSpam
                    typeExpanded = false
                    onChange("com.atproto.report.reasonType#spam")
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.others)) },
                onClick = {
                    typeText = typeTextOthers
                    typeExpanded = false
                    onChange("com.atproto.report.reasonType#other")
                }
            )
        }
    }
}

@Composable
fun ReportDialog(feedViewPost: FeedViewPost, onDismissRequest: () -> Unit) {
    val viewModel: PostViewModel = viewModel()
    val context = LocalContext.current
    val reportedMessage = stringResource(id = R.string.dialog_reported)

    var reason by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("com.atproto.report.reasonType#spam") }

    ConfirmDialog(body = {
        Text(text = stringResource(id = R.string.dialog_report_message))
        Spacer(modifier = Modifier.height(24.dp))
        EmbedPost(viewPost = feedViewPost)
        Spacer(modifier = Modifier.height(24.dp))
        TypeDropdownMenu { type = it }
        TextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text(stringResource(id = R.string.dialog_report_reason)) },
            placeholder = { Text(text = stringResource(id = R.string.optional)) },
            maxLines = 8,
            modifier = Modifier
                .height(160.dp)
                .fillMaxWidth()
        )
    }, action = {
            TextButton(onClick = {
                viewModel.reportPost(
                    feedViewPost,
                    type,
                    reason,
                    onSuccess = {
                        Toast.makeText(context, reportedMessage, Toast.LENGTH_LONG).show()
                        onDismissRequest()
                    },
                    onError = {
                        Log.d(SeiunApplication.TAG, it.toString())
                        onDismissRequest()
                        Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                    }
                )
            }) {
                Text(stringResource(id = R.string.report))
            }
        }, onDismissRequest = onDismissRequest)
}
