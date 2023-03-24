package io.github.akiomik.seiun.model.app.bsky.feed

import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef
import java.util.*

data class Like(
    val subject: StrongRef,
    val createdAt: Date
)
