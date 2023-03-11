package io.github.akiomik.seiun.model.app.bsky.report

import com.squareup.moshi.Json

data class RepoRefOrRecordRef(
    @Json(name = "\$type")
    val type: String,
    val did: String? = null, // from com.atproto.repo.repoRef
    val uri: String? = null, // from com.atproto.repo.recordRef
    val cid: String? = null // from com.atproto.repo.recordRef
)
