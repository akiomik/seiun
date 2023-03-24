package io.github.akiomik.seiun.model.app.bsky.actor

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.Blob

@JsonClass(generateAdapter = true)
data class Profile(
    val displayName: String? = null,
    val description: String? = null,
    val avatar: Blob? = null,
    val banner: Blob? = null
)
