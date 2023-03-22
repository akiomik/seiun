package io.github.akiomik.seiun.model.app.bsky.actor

import io.github.akiomik.seiun.model.type.Image

data class UpdateProfileInput(
    val did: String? = null,
    val displayName: String? = null,
    val description: String? = null,
    val avatar: Image? = null,
    val banner: Image? = null
)
