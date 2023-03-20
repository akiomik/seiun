package io.github.akiomik.seiun.model.app.bsky.notification

import com.squareup.moshi.JsonClass

// app.bsky.feed.repost or app.bsky.feed.vote or
@JsonClass(generateAdapter = true)
data class NotificationSubject(
    val uri: String? = null,
    val cid: String? = null,
    val did: String? = null,
    val declarationCid: String? = null
)
