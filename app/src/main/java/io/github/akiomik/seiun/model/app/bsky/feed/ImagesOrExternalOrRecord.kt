package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.embed.External
import io.github.akiomik.seiun.model.app.bsky.embed.ImagesImage
import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef

@JsonClass(generateAdapter = true)
data class ImagesOrExternalOrRecord(
    @Json(name = "\$type")
    val type: String,
    val images: List<ImagesImage>? = null, // from app.bsky.embed.images
    val external: External? = null, // from app.bsky.embed.external
    val record: StrongRef? = null // from app.bsky.embed.record
)
