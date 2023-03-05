package io.github.akiomik.seiun.model

data class RepostParam(
    val did: String,
    val record: RepostRecord,
    val collection: String = "app.bsky.feed.repost",
    val validate: Boolean? = null,
)
