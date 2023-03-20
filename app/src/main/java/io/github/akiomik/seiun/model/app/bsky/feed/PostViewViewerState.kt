package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostViewViewerState(
    val repost: String? = null,
    val upvote: String? = null,
    val downvote: String? = null,
    val muted: Boolean? = null
)
