package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.embed.External
import io.github.akiomik.seiun.model.app.bsky.embed.Image

@JsonClass(generateAdapter = true)
data class ImagesOrExternal(
    @Json(name = "\$type")
    val type: String,
    val images: List<Image>? = null, // from app.bsky.embed.images
    val external: External? = null // from app.bsky.embed.external
)
