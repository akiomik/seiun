package io.github.akiomik.seiun.model.app.bsky.actor

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateProfileOutput(
    val uri: String,
    val cid: String,
    val record: Profile // TODO: unknown
)
