package io.github.akiomik.seiun.model

data class FeedPost(
    val uri: String,
    val cid: String,
    val author: ActorWithInfo,
    val record: FeedPostRecord,
    val replyCount: Int,
    val repostCount: Int,
    val upvoteCount: Int,
    val downvoteCount: Int,
    val indexedAt: String, // TODO: datetime
    val viewer: ViewerState,
    val embed: FeedPostEmbed?,
) {
    fun reposted(uri: String): FeedPost {
        return copy(
            repostCount = repostCount + 1,
            viewer = viewer.copy(repost = uri),
        )
    }

    fun repostCanceled(): FeedPost {
        return copy(
            repostCount = repostCount - 1,
            viewer = viewer.copy(repost = null),
        )
    }

    fun upvoted(uri: String): FeedPost {
        return copy(
            upvoteCount = upvoteCount + 1,
            viewer = viewer.copy(upvote = uri),
        )
    }

    fun upvoteCanceled(): FeedPost {
        return copy(
            upvoteCount = upvoteCount - 1,
            viewer = viewer.copy(upvote = null),
        )
    }

    fun toStrongRef(): StrongRef {
        return StrongRef(uri = uri, cid = cid)
    }
}