package io.github.akiomik.seiun.model.type

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Image(
    val cid: String,
    val mimeType: String
)
