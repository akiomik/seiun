package io.github.akiomik.seiun.model

import com.squareup.moshi.Json

data class FeedPostRecord(
    val text: String,
    @Json(name = "\$type")
    val type: String = "app.bsky.feed.post",
    val createdAt: String)