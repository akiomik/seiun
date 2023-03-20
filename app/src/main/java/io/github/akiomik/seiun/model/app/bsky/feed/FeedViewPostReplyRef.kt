package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedViewPostReplyRef(
    val root: PostView,
    val parent: PostView
)
