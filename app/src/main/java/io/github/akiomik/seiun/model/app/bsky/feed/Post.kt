package io.github.akiomik.seiun.model.app.bsky.feed

import java.time.Instant
import java.time.format.DateTimeFormatter

data class Post(
    val text: String,
    val createdAt: String, // TODO: datetime
    val entities: List<PostEntity>? = null,
    val reply: PostReplyRef? = null,
    val embed: ImagesOrExternal? = null,
) {
    fun createdAtAsInstant(): Instant {
        return Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(createdAt))
    }
}