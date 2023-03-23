package io.github.akiomik.seiun.model.app.bsky.graph

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MuteActorInput(
    val actor: String
)
