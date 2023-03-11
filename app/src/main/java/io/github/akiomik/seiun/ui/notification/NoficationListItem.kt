package io.github.akiomik.seiun.ui.notification

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.model.app.bsky.notification.Notification

const val DATETIME_FORMAT = "yyyy/MM/dd HH:mm"

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
    val createdAt = DateFormat.format(
        DATETIME_FORMAT,
        notification.record.createdAtAsInstant().toEpochMilli()
    )

    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(notification = notification)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                stringResource(
                    R.string.notification_liked,
                    notification.author.displayName ?: notification.author.handle
                )
            )
            Text(
                text = createdAt.toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun RepostItem(notification: Notification) {
    val createdAt = DateFormat.format(
        DATETIME_FORMAT,
        notification.record.createdAtAsInstant().toEpochMilli()
    )

    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(notification = notification)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                stringResource(
                    R.string.notification_reposted,
                    notification.author.displayName ?: notification.author.handle
                )
            )
            Text(
                text = createdAt.toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun FollowItem(notification: Notification) {
    val createdAt = DateFormat.format(
        DATETIME_FORMAT,
        notification.record.createdAtAsInstant().toEpochMilli()
    )

    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(notification = notification)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                stringResource(
                    R.string.notification_followed,
                    notification.author.displayName ?: notification.author.handle
                )
            )
            Text(
                text = createdAt.toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun InviteItem(notification: Notification) {
    val createdAt = DateFormat.format(
        DATETIME_FORMAT,
        notification.record.createdAtAsInstant().toEpochMilli()
    )

    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(notification = notification)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                stringResource(
                    R.string.notification_invited,
                    notification.author.displayName ?: notification.author.handle
                )
            )
            Text(
                text = createdAt.toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun MentionItem(notification: Notification) {
    val createdAt = DateFormat.format(
        DATETIME_FORMAT,
        notification.record.createdAtAsInstant().toEpochMilli()
    )

    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(notification = notification)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                stringResource(
                    R.string.notification_mentioned,
                    notification.author.displayName ?: notification.author.handle
                )
            )
            Text(
                text = createdAt.toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ReplyItem(notification: Notification) {
    val createdAt = DateFormat.format(
        DATETIME_FORMAT,
        notification.record.createdAtAsInstant().toEpochMilli()
    )

    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(notification = notification)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                stringResource(
                    R.string.notification_replied,
                    notification.author.displayName ?: notification.author.handle
                )
            )
            Text(
                text = createdAt.toString(),
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun NotificationListItem(notification: Notification) {
    when (notification.reason) {
        "vote" -> VoteItem(notification = notification)
        "repost" -> RepostItem(notification = notification)
        "follow" -> FollowItem(notification = notification)
        "invite" -> InviteItem(notification = notification)
        "mention" -> MentionItem(notification = notification)
        "reply" -> ReplyItem(notification = notification)
        else -> {}
    }
}
