package io.github.akiomik.seiun.model.app.bsky.richtext

data class Facet(
    val index: FacetByteSlice,
    val features: List<FacetMentionOrFacetLink> // TODO: union
)
