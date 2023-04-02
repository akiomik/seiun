package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef

@JsonClass(generateAdapter = true)
data class StrongRefOrRecord(
    val uri: String? = null, // from com.atproto.repo.strongRef
    val cid: String? = null, // from com.atproto.repo.strongRef
    val record: StrongRef? = null // from app.bsky.embed.record
)
