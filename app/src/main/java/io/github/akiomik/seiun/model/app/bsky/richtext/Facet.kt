package io.github.akiomik.seiun.model.app.bsky.richtext

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Facet(
    val index: FacetByteSlice,
    val features: List<FacetMentionOrFacetLink> // TODO: union
)
