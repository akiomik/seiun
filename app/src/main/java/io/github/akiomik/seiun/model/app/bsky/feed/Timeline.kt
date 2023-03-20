package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Timeline(
    val feed: List<FeedViewPost>,
    val cursor: String? = null
)
