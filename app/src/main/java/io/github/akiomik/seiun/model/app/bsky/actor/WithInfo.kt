package io.github.akiomik.seiun.model.app.bsky.actor

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.system.DeclRef

@JsonClass(generateAdapter = true)
data class WithInfo(
    val did: String,
    val declaration: DeclRef,
    val handle: String,
    val displayName: String? = null,
    val avatar: String? = null,
    val viewer: ViewerState? = null
)
