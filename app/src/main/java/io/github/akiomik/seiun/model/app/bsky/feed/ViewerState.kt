package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ViewerState(
    val repost: String? = null,
    val like: String? = null
)
