package io.github.akiomik.seiun.model.app.bsky.actor

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfileViewBasic(
    val did: String,
    val handle: String,
    val displayName: String? = null,
    val avatar: String? = null,
    val viewer: ViewerState? = null
)
