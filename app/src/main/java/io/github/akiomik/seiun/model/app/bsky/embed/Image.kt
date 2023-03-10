package io.github.akiomik.seiun.model.app.bsky.embed

import io.github.akiomik.seiun.model.type.Image as ImageType

data class Image(
    val image: ImageType,
    val alt: String,
)