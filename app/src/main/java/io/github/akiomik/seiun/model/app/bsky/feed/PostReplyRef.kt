package io.github.akiomik.seiun.model.app.bsky.feed

import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef

data class PostReplyRef(
    val root: StrongRef,
    val parent: StrongRef
)
