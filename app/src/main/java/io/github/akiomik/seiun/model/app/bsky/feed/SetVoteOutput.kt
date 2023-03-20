package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SetVoteOutput(
    val upvote: String?,
    val downvote: String?
)
