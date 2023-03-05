package io.github.akiomik.seiun.model

data class FeedViewPost(
    val post: FeedPost,
    val reply: ReplyRef? = null,
    val reason: FeedViewPostReason? = null
)
