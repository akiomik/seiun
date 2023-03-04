package io.github.akiomik.seiun.model

import com.squareup.moshi.Json
import java.util.*

data class FeedViewPostReason(
    @Json(name = "\$type")
    val type: String,
    val by: ActorWithInfo,
    val indexedAt: String // TODO: Use Date
    )
