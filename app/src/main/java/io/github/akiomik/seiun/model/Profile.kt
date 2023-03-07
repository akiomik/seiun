package io.github.akiomik.seiun.model

import java.io.FileDescriptor

data class Profile(
    val did: String,
    // val declaration: DeclRef, // TODO
    val handle: String,
    val creator: String,
    val followersCount: Int,
    val followsCount: Int,
    val postsCount: Int,
    val membersCount: Int?,
    val displayName: String?,
    val description: String?,
    val avatar: String?,
    val banner: String?,
    // val myState: MyState? // TODO
)
