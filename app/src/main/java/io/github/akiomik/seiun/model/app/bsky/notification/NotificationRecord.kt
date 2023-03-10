package io.github.akiomik.seiun.model.app.bsky.notification

import com.squareup.moshi.Json
import io.github.akiomik.seiun.model.app.bsky.feed.PostReplyRef
import io.github.akiomik.seiun.model.app.bsky.feed.VoteDirection
import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef
import java.time.Instant
import java.time.format.DateTimeFormatter

// NOTE: app.bsky.graph.follow or app.bsky.feed.vote or app.bsky.feed.repost
data class NotificationRecord(
    @Json(name = "\$type")
    val type: String,
    val createdAt: String, // TODO: datetime
    val text: String? = null,
    val reply: PostReplyRef? = null,
    val direction: VoteDirection? = null,
    val subject: NotificationSubject? = null
) {
    fun createdAtAsInstant(): Instant {
        return Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(createdAt))
    }
}
