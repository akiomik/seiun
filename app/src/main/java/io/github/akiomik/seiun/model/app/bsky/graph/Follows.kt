package io.github.akiomik.seiun.model.app.bsky.graph

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileViewBasic

@JsonClass(generateAdapter = true)
data class Follows(
    val subject: ProfileViewBasic,
    val follows: List<ProfileViewBasic>,
    val cursor: String? = null
)
