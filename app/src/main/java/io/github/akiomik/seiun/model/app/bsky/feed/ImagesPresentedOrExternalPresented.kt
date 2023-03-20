package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.embed.PresentedExternal
import io.github.akiomik.seiun.model.app.bsky.embed.PresentedImage

@JsonClass(generateAdapter = true)
data class ImagesPresentedOrExternalPresented(
    @Json(name = "\$type")
    val type: String,
    val images: List<PresentedImage>? = null, // from app.bsky.embed.images#presented
    val external: PresentedExternal? = null // app.bsky.embed.external#presented
)
