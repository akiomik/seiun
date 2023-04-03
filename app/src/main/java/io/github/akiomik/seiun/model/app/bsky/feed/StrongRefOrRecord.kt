package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass

// FIXME: May have embed, author, record, viewer, indexedAt, likeCount, replyCount, repostCount?
@JsonClass(generateAdapter = true)
data class StrongRefOrRecord(
    val uri: String? = null, // from com.atproto.repo.strongRef
    val cid: String? = null // from com.atproto.repo.strongRef
    // FIXME: record may app.bsky.feed.post?
//    val record: StrongRef? = null // from app.bsky.embed.record
)
