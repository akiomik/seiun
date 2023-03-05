package io.github.akiomik.seiun.model

import com.squareup.moshi.JsonClass

data class ActorWithInfo(
    val did: String,
//    val declaration: Ddcl,
    val handle: String,
    val displayName: String?,
    val avatar: String?,
//    val viewer: Viewer?
)
