package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.Json
import io.github.akiomik.seiun.model.app.bsky.actor.RefWithInfo

data class ReasonTrendOrReasonRepost(
    @Json(name = "\$type")
    val type: String,
    val by: RefWithInfo,
    val indexedAt: String // TODO: datetime
)
