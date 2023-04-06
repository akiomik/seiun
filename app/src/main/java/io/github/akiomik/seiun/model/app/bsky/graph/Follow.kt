package io.github.akiomik.seiun.model.app.bsky.graph

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.Record
import java.util.*

@JsonClass(generateAdapter = true)
data class Follow(
    val subject: String,
    override val createdAt: Date
) : Record
