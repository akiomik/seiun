package io.github.akiomik.seiun.model.app.bsky.feed

data class PostEntity(
    val index: PostEntityTextSlice,
    val type: String,
    val value: String,
)