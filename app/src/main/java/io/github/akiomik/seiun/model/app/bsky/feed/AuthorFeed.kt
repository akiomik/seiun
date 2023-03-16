package io.github.akiomik.seiun.model.app.bsky.feed

data class AuthorFeed(
    val feed: List<FeedViewPost>,
    val cursor: String? = null
)
