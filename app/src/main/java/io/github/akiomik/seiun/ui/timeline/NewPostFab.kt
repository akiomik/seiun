package io.github.akiomik.seiun.ui.timeline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 新規投稿するFloatingActionButtonの設定
@Composable
fun NewPostFab() {
    var showPostForm by remember { mutableStateOf(false) }
    if (showPostForm) {
        NewPostForm { showPostForm = false }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                modifier = Modifier
                    .padding(16.dp)
                    .align(alignment = Alignment.BottomEnd),
                onClick = {
                    showPostForm = true
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create new post")
            }
        }
    }
}