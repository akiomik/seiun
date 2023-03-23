package io.github.akiomik.seiun.model.app.bsky.richtext

import com.squareup.moshi.Json

data class FacetMentionOrFacetLink(
    @Json(name = "\$type")
    val type: String,
    val did: String? = null, // from app.bsky.richtext.facet#mention
    val uri: String? = null // from app.bsky.richtext.facet#link
)
