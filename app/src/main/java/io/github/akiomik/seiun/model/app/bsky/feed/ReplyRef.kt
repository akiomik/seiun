package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReplyRef(
    val root: PostView,
    val parent: PostView
)
