package io.github.akiomik.seiun.model.app.bsky.report

import com.squareup.moshi.Json

data class RepoRefOrStrongRef(
    @Json(name = "\$type")
    val type: String,
    val did: String? = null, // from com.atproto.repo.repoRef
    val uri: String? = null, // from com.atproto.repo.strongRef
    val cid: String? = null // from com.atproto.repo.strongRef
)
