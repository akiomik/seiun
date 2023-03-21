package io.github.akiomik.seiun.model.app.bsky.graph

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.Ref
import java.util.*

@JsonClass(generateAdapter = true)
data class Follow(
    val subject: Ref,
    val createdAt: Date
)
