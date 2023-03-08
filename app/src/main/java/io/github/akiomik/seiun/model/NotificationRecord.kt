package io.github.akiomik.seiun.model

import com.squareup.moshi.Json
import java.time.Instant
import java.time.format.DateTimeFormatter

// NOTE: unknown type
data class NotificationRecord(
    @Json(name = "\$type")
    val type: String,
    val createdAt: String, // TODO: datetime
    val text: String?,
    val reply: ReplyRefForNotification?,
    val direction: VoteDirection?,
    // TODO: Add subject (StrongRef or ? or null)
    //    subject: {
    //        cid: 'bafyreihsqgmuusmcbtxi5feabv6rlrkhxoyxgdfu6sy3truntnzm6ovily',
    //        uri: 'at://did:plc:j5cxpczcvzajlxhfuq7abivp/app.bsky.feed.post/3jqbsb76dcs2g'
    //    },
) {
    fun createdAtAsInstant(): Instant {
        return Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(createdAt))
    }
}