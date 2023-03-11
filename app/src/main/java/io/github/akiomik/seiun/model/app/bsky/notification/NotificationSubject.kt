package io.github.akiomik.seiun.model.app.bsky.notification

// app.bsky.feed.repost or app.bsky.feed.vote or
data class NotificationSubject(
    val uri: String? = null,
    val cid: String? = null,
    val did: String? = null,
    val declarationCid: String? = null
)
