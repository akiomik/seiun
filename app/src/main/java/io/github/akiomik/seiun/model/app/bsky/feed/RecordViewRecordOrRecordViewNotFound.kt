package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.Json
import io.github.akiomik.seiun.model.app.bsky.actor.WithInfo
import java.util.*

data class RecordViewRecordOrRecordViewNotFound(
    @Json(name = "\$type")
    val type: String,
    // from app.bsky.embed.record#viewRecord
    val uri: String, // is used by app.bsky.embed.record#viewNotFound too
    val cid: String,
    val author: WithInfo,
    // val record: Record, // TODO unknown
    val indexedAt: Date,
    val embeds: List<ImagesViewOrExternalViewOrRecordView>? = null
)
