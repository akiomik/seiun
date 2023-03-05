package io.github.akiomik.seiun.model

data class CreatePostParam(
    val did: String,
    val record: FeedPostRecord,
    val collection: String = "app.bsky.feed.post",
    val validate: Boolean? = null,
)