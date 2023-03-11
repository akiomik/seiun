package io.github.akiomik.seiun.ui.dialog

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import io.github.akiomik.seiun.ui.embed.EmbedPost
import io.github.akiomik.seiun.viewmodel.TimelineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDialog(feedViewPost: FeedViewPost, onDismissRequest: () -> Unit) {
    val viewModel: TimelineViewModel = viewModel()
    val context = LocalContext.current
    val typeTextSpam = stringResource(id = R.string.spam)
    val typeTextOthers = stringResource(id = R.string.others)
    val reportedMessage = stringResource(id = R.string.timeline_reported)

    var typeExpanded by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("com.atproto.report.reasonType#spam") }
    var typeText by remember { mutableStateOf(typeTextSpam) }

    AlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(id = R.string.timeline_report_message))
                Spacer(modifier = Modifier.height(24.dp))
                EmbedPost(viewPost = feedViewPost)
                Spacer(modifier = Modifier.height(24.dp))
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        readOnly = true,
                        value = typeText,
                        label = { Text(stringResource(R.string.timeline_report_type)) },
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
                                type = "com.atproto.report.reasonType#spam"
                                typeText = typeTextSpam
                                typeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.others)) },
                            onClick = {
                                type = "com.atproto.report.reasonType#other"
                                typeText = typeTextOthers
                                typeExpanded = false
                            }
                        )
                    }
                }
                TextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(stringResource(id = R.string.timeline_report_reason)) },
                    placeholder = { Text(text = stringResource(id = R.string.optional)) },
                    maxLines = 8,
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(id = R.string.cancel))
                    }

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
                }
            }
        }
    }
}
