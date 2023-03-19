package io.github.akiomik.seiun.ui.user

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.viewmodels.UserFeedViewModel

@Composable
fun LoadingIndicator() {
    val viewModel: UserFeedViewModel = viewModel()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }

    LaunchedEffect(key1 = true) {
        viewModel.loadMorePosts(onError = {
            Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
        })
    }
}
