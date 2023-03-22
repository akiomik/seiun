package io.github.akiomik.seiun.model.app.bsky.graph

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.RefWithInfo

@JsonClass(generateAdapter = true)
data class Followers(
    val subject: RefWithInfo,
    val followers: List<RefWithInfo>,
    val cursor: String? = null
)
