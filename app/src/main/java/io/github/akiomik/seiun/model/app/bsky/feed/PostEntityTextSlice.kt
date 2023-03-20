package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostEntityTextSlice(
    val start: Int,
    val end: Int
)
