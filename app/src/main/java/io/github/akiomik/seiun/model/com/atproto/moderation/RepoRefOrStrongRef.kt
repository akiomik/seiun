package io.github.akiomik.seiun.model.com.atproto.moderation

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RepoRefOrStrongRef(
    @Json(name = "\$type")
    val type: String,
    val did: String? = null, // from com.atproto.repo.repoRef
    val uri: String? = null, // from com.atproto.repo.strongRef
    val cid: String? = null // from com.atproto.repo.strongRef
)
