package io.github.akiomik.seiun.model.app.bsky.actor

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.system.DeclRef

@JsonClass(generateAdapter = true)
data class Profile(
    val did: String,
    val declaration: DeclRef,
    val handle: String,
    val creator: String,
    val followersCount: Int,
    val followsCount: Int,
    val postsCount: Int,
    val membersCount: Int? = null,
    val displayName: String? = null,
    val description: String? = null,
    val avatar: String? = null,
    val banner: String? = null,
    val myState: MyState? = null
)
