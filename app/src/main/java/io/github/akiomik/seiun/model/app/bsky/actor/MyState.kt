package io.github.akiomik.seiun.model.app.bsky.actor

data class MyState(
    val follow: String?,
    val member: String?,
    val muted: Boolean,
)