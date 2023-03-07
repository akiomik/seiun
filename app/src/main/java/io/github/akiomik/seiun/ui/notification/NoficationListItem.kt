package io.github.akiomik.seiun.ui.notification

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.akiomik.seiun.model.Notification
import io.github.akiomik.seiun.model.NotificationReason
import java.time.Instant

@Composable
private fun Avatar(notification: Notification) {
    AsyncImage(
        model = notification.author.avatar,
        contentDescription = null,
        modifier = Modifier
            .width(50.dp)
            .height(50.dp)
            .clip(CircleShape)
    )
}

@Composable
private fun VoteItem(notification: Notification) {
    val createdAt = Instant.parse(notification.record.createdAt)

    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(notification = notification)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text("${notification.author.displayName} liked your post")
            Text(
                text = DateFormat.format("yyyy/mm/dd hh:mm", createdAt.toEpochMilli()).toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun RepostItem(notification: Notification) {
    val createdAt = Instant.parse(notification.record.createdAt)

    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(notification = notification)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text("${notification.author.displayName} reposted your post")
            Text(
                text = DateFormat.format("yyyy/mm/dd hh:mm", createdAt.toEpochMilli()).toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun FollowItem(notification: Notification) {
    val createdAt = Instant.parse(notification.record.createdAt)

    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(notification = notification)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text("${notification.author.displayName} is followed you")
            Text(
                text = DateFormat.format("yyyy/mm/dd hh:mm", createdAt.toEpochMilli()).toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun InviteItem(notification: Notification) {
    val createdAt = Instant.parse(notification.record.createdAt)

    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(notification = notification)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text("${notification.author.displayName} invited you")
            Text(
                text = DateFormat.format("yyyy/mm/dd hh:mm", createdAt.toEpochMilli()).toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun MentionItem(notification: Notification) {
    val createdAt = Instant.parse(notification.record.createdAt)

    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(notification = notification)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text("${notification.author.displayName} mentioned you")
            Text(
                text = DateFormat.format("yyyy/mm/dd hh:mm", createdAt.toEpochMilli()).toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ReplyItem(notification: Notification) {
    val createdAt = Instant.parse(notification.record.createdAt)

    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(notification = notification)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text("${notification.author.displayName} replied to you")
            Text(
                text = DateFormat.format("yyyy/mm/dd hh:mm", createdAt.toEpochMilli()).toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun NotificationListItem(notification: Notification) {
    when (notification.reason) {
        NotificationReason.vote -> VoteItem(notification = notification)
        NotificationReason.repost -> RepostItem(notification = notification)
        NotificationReason.follow -> FollowItem(notification = notification)
        NotificationReason.invite -> InviteItem(notification = notification)
        NotificationReason.mention -> MentionItem(notification = notification)
        NotificationReason.reply -> ReplyItem(notification = notification)
    }
}
