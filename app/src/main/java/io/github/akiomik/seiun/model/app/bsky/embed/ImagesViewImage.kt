package io.github.akiomik.seiun.model.app.bsky.embed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImagesViewImage(
    val thumb: String,
    val fullsize: String,
    val alt: String
)
