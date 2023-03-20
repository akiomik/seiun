package io.github.akiomik.seiun.model.app.bsky.embed

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.Image as ImageType

@JsonClass(generateAdapter = true)
data class Image(
    val image: ImageType,
    val alt: String
)
