package io.github.akiomik.seiun.model.app.bsky.system

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeclRef(
    val cid: String,
    val actorType: String
)
