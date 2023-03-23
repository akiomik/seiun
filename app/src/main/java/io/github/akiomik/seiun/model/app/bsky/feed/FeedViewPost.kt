package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedViewPost(
    val post: PostView,
    val reply: ReplyRef? = null,
    val reason: ReasonRepost? = null // TODO: union type
) {
    fun id(): String = reason?.id()?.let { "${post.cid}-$it" } ?: post.cid
}
