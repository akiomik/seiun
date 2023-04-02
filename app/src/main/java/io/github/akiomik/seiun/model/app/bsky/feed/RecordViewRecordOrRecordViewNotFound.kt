package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.WithInfo
import java.util.*

@JsonClass(generateAdapter = true)
data class RecordViewRecordOrRecordViewNotFound(
//    @Json(name = "\$type")
//    val type: String,
    // from app.bsky.embed.record#viewRecord
    val uri: String? = null, // is used by app.bsky.embed.record#viewNotFound too
    val cid: String? = null,
    val author: WithInfo? = null,
    // val record: Record, // TODO unknown
    val indexedAt: Date? = null,
    val embeds: List<ImagesViewOrExternalViewOrRecordViewOrRecordWithMediaView>? = null
)
