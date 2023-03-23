package io.github.akiomik.seiun.model.app.bsky.richtext

data class Facet(
    val index: FacetTextSlice,
    val value: FacetMentionOrFacetLink // TODO: union
)
