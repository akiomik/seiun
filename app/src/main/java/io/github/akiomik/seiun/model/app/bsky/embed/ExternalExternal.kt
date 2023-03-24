package io.github.akiomik.seiun.model.app.bsky.embed

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.Blob

@JsonClass(generateAdapter = true)
data class ExternalExternal(
    val uri: String,
    val title: String,
    val description: String,
    val thumb: Blob? = null
)
