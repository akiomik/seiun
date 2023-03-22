package io.github.akiomik.seiun.model.app.bsky.actor

data class UpdateProfileOutput(
    val uri: String,
    val cid: String,
    val record: Profile // TODO: unknown
)
