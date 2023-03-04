package io.github.akiomik.seiun.model

import com.squareup.moshi.Json

data class FeedPostRecord(
    val text: String,
    @Json(name = "\$type")
    val type: String,
    val createdAt: String)