package io.github.akiomik.seiun.model.app.bsky.embed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExternalViewExternal(
    val uri: String,
    val title: String,
    val description: String,
    val thumb: String? = null
)
