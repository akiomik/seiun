package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostEntity(
    val index: PostEntityTextSlice,
    val type: String,
    val value: String
)
