package io.github.akiomik.seiun.model.app.bsky.report

data class ReportCreateInput(
    val reasonType: String,
    val subject: RepoRefOrRecordRef,
    val reason: String? = null
)
