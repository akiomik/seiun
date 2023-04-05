package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.embed.External
import io.github.akiomik.seiun.model.app.bsky.embed.Images
import io.github.akiomik.seiun.model.app.bsky.embed.Record
import io.github.akiomik.seiun.model.app.bsky.embed.RecordWithMedia
import io.github.akiomik.seiun.model.app.bsky.richtext.Facet
import io.github.akiomik.seiun.model.type.Union4
import java.util.*

@JsonClass(generateAdapter = true)
data class Post(
    val text: String,
    val createdAt: Date,
    val entities: List<PostEntity>? = null,
    val facets: List<Facet>? = null,
    val reply: PostReplyRef? = null,
    val embed: Union4<Images, External, Record, RecordWithMedia>? = null
)
