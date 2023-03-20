package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedViewPost(
    val post: PostView,
    val reply: FeedViewPostReplyRef? = null,
    val reason: ReasonTrendOrReasonRepost? = null
) {
    fun id(): String = reason?.id()?.let { "${post.cid}-$it" } ?: post.cid
}
