package io.github.akiomik.seiun.ui.timeline

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ChatBubbleOutline
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.Favorite
import androidx.compose.material.icons.sharp.FavoriteBorder
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material.icons.sharp.SyncAlt
import androidx.compose.material.icons.sharp.VolumeMute
import androidx.compose.material.icons.sharp.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.model.app.bsky.embed.PresentedImage
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import io.github.akiomik.seiun.ui.dialog.DeleteDialog
import io.github.akiomik.seiun.ui.dialog.MuteDialog
import io.github.akiomik.seiun.ui.dialog.ReportDialog
import io.github.akiomik.seiun.ui.theme.Green700
import io.github.akiomik.seiun.ui.theme.Red700
import io.github.akiomik.seiun.viewmodel.TimelineViewModel

@Composable
private fun RepostText(viewPost: FeedViewPost) {
    Box(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = stringResource(
                id = R.string.timeline_reposted,
                viewPost.reason?.by?.displayName ?: viewPost.reason?.by?.handle ?: ""
            ),
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun ReplyText(viewPost: FeedViewPost) {
    Box(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = stringResource(
                id = R.string.timeline_replying,
                viewPost.reply?.parent?.author?.displayName
                    ?: viewPost.reply?.parent?.author?.handle ?: ""
            ),
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

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
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DisplayName(viewPost = viewPost)
        Handle(viewPost = viewPost)
    }
}

@Composable
private fun ReplyIndicator(viewPost: FeedViewPost) {
    var showPostForm by remember { mutableStateOf(false) }

    if (showPostForm) {
        NewPostFormModal(viewPost) { showPostForm = false }
    }

    TextButton(
        modifier = Modifier.width(64.dp),
        onClick = { showPostForm = true; }
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                viewModel.cancelVote(post = viewPost.post, onError = {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                })
            } else {
                viewModel.upvote(post = viewPost.post, onError = {
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

@Composable
fun MenuButton(viewPost: FeedViewPost) {
    val viewModel: TimelineViewModel = viewModel()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showMuteDialog by remember { mutableStateOf(false) }
    val profile by viewModel.profile.observeAsState()

    TextButton(onClick = { showMenu = true }) {
        Icon(
            painter = rememberVectorPainter(Icons.Sharp.MoreVert),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
    }

    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        if (viewPost.post.author.did == profile?.did) {
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.delete)) },
                onClick = {
                    showMenu = false
                    showDeleteDialog = true
                },
                leadingIcon = {
                    Icon(
                        Icons.Sharp.Delete,
                        contentDescription = null
                    )
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.mute)) },
                onClick = {
                    showMenu = false
                    showMuteDialog = true
                },
                leadingIcon = {
                    Icon(
                        Icons.Sharp.VolumeMute,
                        contentDescription = null
                    )
                }
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.report)) },
            onClick = {
                showMenu = false
                showReportDialog = true
            },
            leadingIcon = {
                Icon(
                    Icons.Sharp.Warning,
                    contentDescription = null
                )
            }
        )
    }

    if (showDeleteDialog) {
        DeleteDialog(
            feedViewPost = viewPost,
            onDismissRequest = { showDeleteDialog = false }
        )
    } else if (showReportDialog) {
        ReportDialog(
            feedViewPost = viewPost,
            onDismissRequest = { showReportDialog = false }
        )
    } else if (showMuteDialog) {
        MuteDialog(
            actor = viewPost.post.author,
            onDismissRequest = { showMuteDialog = false }
        )
    }
}

@Composable
private fun FeedPostContent(viewPost: FeedViewPost) {
    val createdAt = DateFormat.format(
        "yyyy/MM/dd HH:mm",
        viewPost.post.record.createdAt.toInstant().toEpochMilli()
    )

    Column {
        NameRow(viewPost = viewPost)

        if (viewPost.post.record.text.isNotEmpty()) {
            SelectionContainer {
                Text(text = viewPost.post.record.text, modifier = Modifier.padding(top = 8.dp))
            }
        }

        ImageTile(viewPost)

        Row(
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReplyIndicator(viewPost = viewPost)
            RepostIndicator(viewPost = viewPost)
            UpvoteIndicator(viewPost = viewPost)
            MenuButton(viewPost = viewPost)
        }
        Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = createdAt.toString(),
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun ImageTile(viewPost: FeedViewPost) {
    var showImagePager by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }

    val paddingTop = 16.dp
    val maxHeight = 240.dp

    viewPost.post.embed?.images?.let { images ->
        if (images.size == 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = paddingTop)
            ) {
                AsyncImage(
                    model = images[0].thumb,
                    contentDescription = images[0].alt,
                    modifier = Modifier
                        .height(maxHeight)
                        .clickable {
                            showImagePager = true
                            selectedIndex = 0
                        },
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            val height = if (images.size > 2) {
                maxHeight / 2
            } else {
                maxHeight
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier
                    .height(maxHeight + 16.dp) // NOTE: Avoid inner scroll
                    .padding(top = paddingTop)
            ) {
                items(images.withIndex().toList()) { (index, image) ->
                    AsyncImage(
                        model = image.thumb,
                        contentDescription = image.alt,
                        modifier = Modifier
                            .height(height)
                            .clickable {
                                showImagePager = true
                                selectedIndex = index
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        if (showImagePager) {
            ImagePager(
                images = images,
                initialIndex = selectedIndex,
                onDismissRequest = { showImagePager = false }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePager(images: List<PresentedImage>, initialIndex: Int, onDismissRequest: () -> Unit) {
    val pagerState = rememberPagerState(initialIndex)

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            modifier = Modifier.wrapContentSize(),
            shape = MaterialTheme.shapes.large
        ) {
            HorizontalPager(
                pageCount = images.size,
                state = pagerState,
                modifier = Modifier.wrapContentSize()
            ) { page ->
                AsyncImage(
                    model = images[page].fullsize,
                    contentDescription = null,
                    modifier = Modifier.wrapContentSize()
                )
            }
        }
    }
}

@Composable
fun FeedPost(viewPost: FeedViewPost) {
    Column(modifier = Modifier.padding(14.dp)) {
        if (viewPost.reason?.type == "app.bsky.feed.feedViewPost#reasonRepost") {
            RepostText(viewPost = viewPost)
        } else if (viewPost.reply != null) {
            ReplyText(viewPost = viewPost)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Avatar(viewPost = viewPost)
            FeedPostContent(viewPost = viewPost)
        }
    }
}
