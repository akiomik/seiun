package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.richtext.Facet
import java.util.*

@JsonClass(generateAdapter = true)
data class Post(
    val text: String,
    val createdAt: Date,
    val entities: List<PostEntity>? = null,
    val facets: List<Facet>? = null,
    val reply: PostReplyRef? = null,
    val embed: ImagesOrExternalOrRecord? = null // TODO: union type
)
