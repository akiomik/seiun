package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.embed.ExternalViewExternal
import io.github.akiomik.seiun.model.app.bsky.embed.ImagesViewImage

@JsonClass(generateAdapter = true)
data class ImagesViewOrExternalViewOrRecordView(
    @Json(name = "\$type")
    val type: String,
    val images: List<ImagesViewImage>? = null, // from app.bsky.embed.images#view
    val external: ExternalViewExternal? = null, // from app.bsky.embed.external#view
    val record: RecordViewRecordOrRecordViewNotFound? = null // from app.bsky.embed.record#view
)
