package io.github.akiomik.seiun.model

data class FeedViewPost(
    val post: FeedPost,
    val reply: ReplyRef?,
//    val reason: Either<ReasonTrend, ReasonRepost>?
)
