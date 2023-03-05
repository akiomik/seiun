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
    val viewer: ViewerState
)
