package io.github.akiomik.seiun.model.app.bsky.report

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReportCreateInput(
    val reasonType: String,
    val subject: RepoRefOrRecordRef,
    val reason: String? = null
)
