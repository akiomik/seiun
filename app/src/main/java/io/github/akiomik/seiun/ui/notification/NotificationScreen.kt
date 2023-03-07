package io.github.akiomik.seiun.ui.notification

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.akiomik.seiun.viewmodel.NotificationViewModel

@Composable
private fun LoadingText() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text("Loading")
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun LoadingIndicator() {
    val viewModel: NotificationViewModel = viewModel()
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
        viewModel.loadMoreNotifications(onError = {
            Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
        })
    }
}

@Composable
fun NoNotificationsYetMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.material3.Text("No notifications yet")
    }
}

@Composable
fun NoMoreNotificationsMessage() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        androidx.compose.material3.Text("No more notifications")
    }
}

@Composable
private fun ErrorMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.material3.Text("Failed to get notifications. Please try later")
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NotificationList() {
    val viewModel: NotificationViewModel = viewModel()
    val listState = rememberLazyListState()
    val notifications = viewModel.notifications.observeAsState()
    val isRefreshing = viewModel.isRefreshing.observeAsState()
    val context = LocalContext.current
    val refreshState =
        rememberPullRefreshState(refreshing = isRefreshing.value ?: false, onRefresh = {
            viewModel.refreshNotifications(onError = {
                Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
            })
        })
    val status = viewModel.state.collectAsState().value

    Box(modifier = Modifier.pullRefresh(state = refreshState)) {
        LazyColumn(state = listState) {
            items(notifications.value.orEmpty()) {
                NotificationListItem(it)
                Divider(color = Color.Gray)
            }

            if (status == NotificationViewModel.State.Error) {
                item { ErrorMessage() }
            } else if (viewModel.notifications.value?.size == 0) {
                item { NoNotificationsYetMessage() }
            } else if (viewModel.seenAllNotifications.value == true) {
                item { NoMoreNotificationsMessage() }
            } else {
                item { LoadingIndicator() }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing.value ?: false,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun NotificationScreen() {
    val viewModel: NotificationViewModel = viewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (viewModel.state.collectAsState().value) {
            is NotificationViewModel.State.Loading -> LoadingText()
            is NotificationViewModel.State.Loaded -> NotificationList()
            is NotificationViewModel.State.Error -> NotificationList()
        }
    }
}
