package io.github.akiomik.seiun.model.app.bsky.actor

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Ref(
    val did: String,
    val declarationCid: String
)
