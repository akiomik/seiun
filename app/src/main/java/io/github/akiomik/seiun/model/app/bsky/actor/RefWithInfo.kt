package io.github.akiomik.seiun.model.app.bsky.actor

import io.github.akiomik.seiun.model.app.bsky.system.DeclRef

data class RefWithInfo(
    val did: String,
    val declaration: DeclRef,
    val handle: String,
    val displayName: String? = null,
    val avatar: String? = null,
    val viewer: ViewerState? = null
)