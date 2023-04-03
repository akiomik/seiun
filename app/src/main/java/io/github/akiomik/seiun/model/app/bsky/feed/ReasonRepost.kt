package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileViewBasic
import java.util.*

@JsonClass(generateAdapter = true)
data class ReasonRepost(
    @Json(name = "\$type")
    val type: String,
    val by: ProfileViewBasic,
    val indexedAt: Date
) {
    fun id(): String = "$type-${by.did}-${indexedAt.time}"
}
