package io.github.akiomik.seiun.model

import io.github.akiomik.seiun.model.FeedViewPost

data class Timeline(
    val feed: List<FeedViewPost>,
    val cursor: String?
)
