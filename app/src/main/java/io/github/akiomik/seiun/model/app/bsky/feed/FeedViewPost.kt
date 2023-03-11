package io.github.akiomik.seiun.model.app.bsky.feed

data class FeedViewPost(
    val post: PostView,
    val reply: FeedViewPostReplyRef? = null,
    val reason: ReasonTrendOrReasonRepost? = null
)
