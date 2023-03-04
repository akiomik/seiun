package io.github.akiomik.seiun.model

data class Timeline(
    val feed: List<FeedViewPost>,
    val cursor: String?
)
