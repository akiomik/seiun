package io.github.akiomik.seiun.model.app.bsky.embed

import io.github.akiomik.seiun.model.type.Image as ImageType

data class ExternalExternal(
    val uri: String,
    val title: String,
    val description: String,
    val thumb: ImageType,
)