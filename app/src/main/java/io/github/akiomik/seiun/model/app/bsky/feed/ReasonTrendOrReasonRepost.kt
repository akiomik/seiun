package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.RefWithInfo
import java.util.*

@JsonClass(generateAdapter = true)
data class ReasonTrendOrReasonRepost(
    @Json(name = "\$type")
    val type: String,
    val by: RefWithInfo,
    val indexedAt: Date
) {
    fun id(): String = "$type-${by.did}-${indexedAt.time}"
}
