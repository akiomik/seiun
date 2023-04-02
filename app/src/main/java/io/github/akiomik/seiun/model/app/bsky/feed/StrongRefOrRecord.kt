package io.github.akiomik.seiun.model.app.bsky.feed

import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef

data class StrongRefOrRecord(
    val uri: String? = null, // from com.atproto.repo.strongRef
    val cid: String? = null, // from com.atproto.repo.strongRef
    val record: StrongRef? = null // from app.bsky.embed.record
)
