package io.github.akiomik.seiun.model.com.atproto.moderation

import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class CreateReportOutput(
    val id: Int,
    val reasonType: ReasonType,
    val subject: RepoRefOrStrongRef,
    val reportedBy: String,
    val createdAt: Date,
    val reason: String? = null
)
