package io.github.akiomik.seiun.model.app.bsky.richtext

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class FacetByteSlice(
    val byteStart: Int,
    val byteEnd: Int
)
