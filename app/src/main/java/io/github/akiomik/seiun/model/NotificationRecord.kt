package io.github.akiomik.seiun.model

import com.squareup.moshi.Json

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
)