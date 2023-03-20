package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef

@JsonClass(generateAdapter = true)
data class SetVoteInput(
    val subject: StrongRef,
    val direction: VoteDirection
)
