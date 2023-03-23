package io.github.akiomik.seiun.model.app.bsky.embed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class External(
    val external: ExternalExternal
)
