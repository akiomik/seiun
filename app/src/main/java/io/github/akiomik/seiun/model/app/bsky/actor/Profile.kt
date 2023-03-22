package io.github.akiomik.seiun.model.app.bsky.actor

import io.github.akiomik.seiun.model.type.Image

data class Profile(
    val displayName: String,
    val description: String? = null,
    val avatar: Image? = null,
    val banner: Image? = null
)
