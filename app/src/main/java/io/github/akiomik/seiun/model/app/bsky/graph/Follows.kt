package io.github.akiomik.seiun.model.app.bsky.graph

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.RefWithInfo

@JsonClass(generateAdapter = true)
data class Follows(
    val subject: RefWithInfo,
    val follows: List<RefWithInfo>,
    val cursor: String? = null
)
