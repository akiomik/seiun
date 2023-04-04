package io.github.akiomik.seiun.model.app.bsky.richtext

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.Union2

@JsonClass(generateAdapter = true)
data class Facet(
    val index: FacetByteSlice,
    val features: List<Union2<FacetMention, FacetLink>>
)
