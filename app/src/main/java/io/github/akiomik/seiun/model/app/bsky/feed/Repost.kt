package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef
import io.github.akiomik.seiun.model.type.Record
import java.util.*

@JsonClass(generateAdapter = true)
data class Repost(
    val subject: StrongRef,
    override val createdAt: Date
) : Record
