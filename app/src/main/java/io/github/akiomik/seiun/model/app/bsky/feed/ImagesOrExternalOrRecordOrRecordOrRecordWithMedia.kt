package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.embed.External
import io.github.akiomik.seiun.model.app.bsky.embed.ImagesImage

@JsonClass(generateAdapter = true)
data class ImagesOrExternalOrRecordOrRecordOrRecordWithMedia(
    @Json(name = "\$type")
    val type: String,
    val images: List<ImagesImage>? = null, // from app.bsky.embed.images
    val external: External? = null, // from app.bsky.embed.external
    val record: StrongRefOrRecord? = null, // from app.bsky.embed.record and app.bsky.embed.recordWithMedia // TODO: union
    val media: ImagesOrExternal? = null // from app.bsky.embed.recordWithMedia // TODO: union
)
