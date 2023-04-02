package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.Json
import io.github.akiomik.seiun.model.app.bsky.embed.ExternalViewExternal
import io.github.akiomik.seiun.model.app.bsky.embed.ImagesViewImage

data class ImagesViewOrExternalView(
    @Json(name = "\$type")
    val type: String,
    val images: List<ImagesViewImage>? = null, // from app.bsky.embed.images#view
    val external: ExternalViewExternal? = null // from app.bsky.embed.external#view
)
