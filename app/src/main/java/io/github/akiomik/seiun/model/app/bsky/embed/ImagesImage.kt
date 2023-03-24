package io.github.akiomik.seiun.model.app.bsky.embed

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.Blob

@JsonClass(generateAdapter = true)
data class ImagesImage(
    val image: Blob,
    val alt: String
)
