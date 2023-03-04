package com.example.catpaw.models

data class Timeline(
    val feed: List<FeedViewPost>,
    val cursor: String?
)
