package io.github.akiomik.seiun.ui.embed

import android.text.format.DateFormat
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost

@Composable
private fun Avatar(viewPost: FeedViewPost) {
    AsyncImage(
        model = viewPost.post.author.avatar,
        contentDescription = null,
        modifier = Modifier
            .width(24.dp)
            .height(24.dp)
            .clip(CircleShape)
    )
}

@Composable
private fun DisplayName(viewPost: FeedViewPost) {
    Text(
        text = "${viewPost.post.author.displayName}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Handle(viewPost: FeedViewPost) {
    Text(
        text = "@${viewPost.post.author.handle}",
        style = MaterialTheme.typography.labelMedium,
        color = Color.Gray
    )
}

@Composable
private fun NameRow(viewPost: FeedViewPost) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Avatar(viewPost = viewPost)
        DisplayName(viewPost = viewPost)
        Handle(viewPost = viewPost)
    }
}

@Composable
private fun PostContent(viewPost: FeedViewPost) {
    val createdAt = DateFormat.format(
        "yyyy/MM/dd HH:mm",
        viewPost.post.record.createdAtAsInstant().toEpochMilli()
    )

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NameRow(viewPost = viewPost)
        Text(text = viewPost.post.record.text)
        Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = createdAt.toString(),
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun EmbedPost(viewPost: FeedViewPost) {
    Column(
        modifier = Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = RoundedCornerShape(6.dp)
        )
    ) {
        PostContent(viewPost = viewPost)
    }
}
