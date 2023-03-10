package io.github.akiomik.seiun.model.app.bsky.feed

import io.github.akiomik.seiun.model.app.bsky.actor.RefWithInfo
import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef

data class PostView(
    val uri: String,
    val cid: String,
    val author: RefWithInfo,
    val record: Post, // TODO: unknown
    val replyCount: Int,
    val repostCount: Int,
    val upvoteCount: Int,
    val downvoteCount: Int,
    val indexedAt: String, // TODO: datetime
    val viewer: PostViewViewerState,
    val embed: ImagesPresentedOrExternalPresented? = null
) {
    fun reposted(uri: String): PostView {
        return copy(
            repostCount = repostCount + 1,
            viewer = viewer.copy(repost = uri),
        )
    }

    fun repostCanceled(): PostView {
        return copy(
            repostCount = repostCount - 1,
            viewer = viewer.copy(repost = null),
        )
    }

    fun upvoted(uri: String): PostView {
        return copy(
            upvoteCount = upvoteCount + 1,
            viewer = viewer.copy(upvote = uri),
        )
    }

    fun upvoteCanceled(): PostView {
        return copy(
            upvoteCount = upvoteCount - 1,
            viewer = viewer.copy(upvote = null),
        )
    }

    fun toStrongRef(): StrongRef {
        return StrongRef(uri = uri, cid = cid)
    }
}