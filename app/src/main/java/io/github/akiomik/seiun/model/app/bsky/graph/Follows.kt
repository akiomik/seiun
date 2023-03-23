package io.github.akiomik.seiun.model.app.bsky.graph

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.WithInfo

@JsonClass(generateAdapter = true)
data class Follows(
    val subject: WithInfo,
    val follows: List<WithInfo>,
    val cursor: String? = null
)
