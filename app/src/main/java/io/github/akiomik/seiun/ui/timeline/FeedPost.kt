package io.github.akiomik.seiun.ui.timeline

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ChatBubbleOutline
import androidx.compose.material.icons.sharp.Favorite
import androidx.compose.material.icons.sharp.FavoriteBorder
import androidx.compose.material.icons.sharp.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.akiomik.seiun.model.FeedViewPost
import io.github.akiomik.seiun.ui.theme.Green700
import io.github.akiomik.seiun.ui.theme.Red700
import io.github.akiomik.seiun.viewmodel.TimelineViewModel
import java.time.Instant

// リポストされている投稿
@Composable
private fun RetweetText(viewPost: FeedViewPost) {
    Box(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "Reposted by ${viewPost.reason?.by?.displayName}",
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

// リプライされている投稿
@Composable
private fun ReplyText(viewPost: FeedViewPost) {
    Box(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "Replying to ${viewPost.reply?.parent?.author?.displayName}",
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

// アイコン
@Composable
private fun Avatar(viewPost: FeedViewPost) {
    AsyncImage(
        model = viewPost.post.author.avatar,
        contentDescription = null,
        modifier = Modifier
            .width(50.dp)
            .height(50.dp)
            .clip(CircleShape)
    )
}

// 表示名
@Composable
private fun DisplayName(viewPost: FeedViewPost) {
    Text(
        text = "${viewPost.post.author.displayName}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

// ハンドル
@Composable
private fun Handle(viewPost: FeedViewPost) {
    Text(
        text = "@${viewPost.post.author.handle}",
        style = MaterialTheme.typography.labelMedium,
        color = Color.Gray
    )
}

// 名前(ハンドルと表示名)の表示
@Composable
private fun NameRow(viewPost: FeedViewPost) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DisplayName(viewPost = viewPost)
        Handle(viewPost = viewPost)
    }
}

// リプライの表示
@Composable
private fun ReplyIndicator(viewPost: FeedViewPost) {
    TextButton(
        modifier = Modifier.width(64.dp),
        onClick = { /*TODO*/ }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Sharp.ChatBubbleOutline),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
            Text(text = viewPost.post.replyCount.toString(), color = Color.Gray)
        }
    }
}

// リポストの表示
@Composable
private fun RepostIndicator(viewPost: FeedViewPost) {
    val viewModel: TimelineViewModel = viewModel()
    val reposted = viewPost.post.viewer.repost != null
    val color: Color = if (reposted) {
        Green700
    } else {
        Color.Gray
    }
    val context = LocalContext.current

    TextButton(
        modifier = Modifier.width(64.dp),
        onClick = {
            if (reposted) {
                viewModel.cancelRepost(viewPost.post, onError = {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                })
            } else {
                viewModel.repost(viewPost.post, onError = {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                })
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Sharp.SyncAlt),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(text = viewPost.post.repostCount.toString(), color = color)
        }
    }
}

// Voteしている投稿の表示
@Composable
private fun UpvoteIndicator(viewPost: FeedViewPost) {
    val viewModel: TimelineViewModel = viewModel()
    val upvoted = viewPost.post.viewer.upvote != null
    val color = if (upvoted) {
        Red700
    } else {
        Color.Gray
    }
    val icon = if (upvoted) {
        Icons.Sharp.Favorite
    } else {
        Icons.Sharp.FavoriteBorder
    }
    val context = LocalContext.current

    TextButton(
        modifier = Modifier.width(64.dp),
        onClick = {
            if (upvoted) {
                viewModel.cancelVote(feedPost = viewPost.post, onError = {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                })
            } else {
                viewModel.upvote(feedPost = viewPost.post, onError = {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                })
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = rememberVectorPainter(icon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(text = viewPost.post.upvoteCount.toString(), color = color)
        }
    }
}

// 投稿のリプライ数等の表示
@Composable
private fun FeedPostContent(viewPost: FeedViewPost) {
    val createdAt = Instant.parse(viewPost.post.record.createdAt)

    Column(modifier = Modifier.padding(start = 8.dp)) {
        NameRow(viewPost = viewPost)
        Text(text = viewPost.post.record.text)
        Row(
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReplyIndicator(viewPost = viewPost)
            RepostIndicator(viewPost = viewPost)
            UpvoteIndicator(viewPost = viewPost)
        }
        Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = DateFormat.format("yyyy/mm/dd hh:mm", createdAt.toEpochMilli()).toString(),
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

// 投稿の表示
@Composable
fun FeedPost(viewPost: FeedViewPost) {
    Column(modifier = Modifier.padding(10.dp)) {
        if (viewPost.reason?.type == "app.bsky.feed.feedViewPost#reasonRepost") {
            RetweetText(viewPost = viewPost)
        } else if (viewPost.reply != null) {
            ReplyText(viewPost = viewPost)
        }

        Row {
            Avatar(viewPost = viewPost)
            FeedPostContent(viewPost = viewPost)
        }
    }
}