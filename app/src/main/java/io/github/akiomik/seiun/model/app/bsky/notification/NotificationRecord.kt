package io.github.akiomik.seiun.model.app.bsky.notification

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.feed.PostReplyRef
import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef
import java.util.*

// app.bsky.graph.follow (subject, createdAt) or
// app.bsky.feed.like (subject, createdAt) or
// app.bsky.feed.repost (subject, createdAt) or
// app.bsky.feed.post (text, reply, createdAt)
@JsonClass(generateAdapter = true)
data class NotificationRecord(
    @Json(name = "\$type")
    val type: String,
    val createdAt: Date,
    val text: String? = null,
    val reply: PostReplyRef? = null,
    val subject: StrongRef? = null
)
