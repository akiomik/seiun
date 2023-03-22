package io.github.akiomik.seiun.model.app.bsky.actor

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ViewerState(
    val muted: Boolean? = null,
    val following: String? = null,
    val followedBy: String? = null
)
