package io.github.akiomik.seiun.model

data class ActorWithInfo(
    val did: String,
//    val declaration: Ddcl,
    val handle: String,
    val displayName: String?,
    val avatar: String?,
//    val viewer: Viewer?
)
