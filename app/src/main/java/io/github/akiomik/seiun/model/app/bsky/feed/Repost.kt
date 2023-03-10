package io.github.akiomik.seiun.model.app.bsky.feed

import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef

data class Repost(
    val subject: StrongRef,
    val createdAt: String, // TODO: datetime
)