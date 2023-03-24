package io.github.akiomik.seiun.model.com.atproto.moderation

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateReportInput(
    val reasonType: ReasonType,
    val subject: RepoRefOrStrongRef,
    val reason: String? = null
)
