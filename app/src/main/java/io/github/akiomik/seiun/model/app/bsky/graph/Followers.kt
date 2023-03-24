package io.github.akiomik.seiun.model.app.bsky.graph

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.WithInfo

@JsonClass(generateAdapter = true)
data class Followers(
    val subject: WithInfo,
    val followers: List<WithInfo>,
    val cursor: String? = null
)
