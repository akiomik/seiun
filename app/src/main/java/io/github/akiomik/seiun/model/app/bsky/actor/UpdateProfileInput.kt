package io.github.akiomik.seiun.model.app.bsky.actor

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.Image

@JsonClass(generateAdapter = true)
data class UpdateProfileInput(
    val did: String? = null,
    val displayName: String? = null,
    val description: String? = null,
    val avatar: Image? = null,
    val banner: Image? = null
)
