package io.github.akiomik.seiun.model.app.bsky.actor

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MyState(
    val follow: String?,
    val member: String?,
    val muted: Boolean
)
