package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass

// TODO: Use sealed interface
/* ktlint-disable enum-entry-name-case */
@JsonClass(generateAdapter = false)
enum class VoteDirection {
    up, down, none
}
/* ktlint-enable enum-entry-name-case */
