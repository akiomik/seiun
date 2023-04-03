package io.github.akiomik.seiun.model.app.bsky.actor

import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class ProfileViewDetailed(
    val did: String,
    val handle: String,
    val displayName: String? = null,
    val description: String? = null,
    val avatar: String? = null,
    val banner: String? = null,
    val followersCount: Int? = null,
    val followsCount: Int? = null,
    val postsCount: Int? = null,
    val indexedAt: Date? = null,
    val viewer: ViewerState? = null
)
