package io.github.akiomik.seiun.model.app.bsky.report

data class ReportCreateOutput(
    val id: Int,
    val reasonType: String,
    val subject: RepoRefOrStrongRef,
    val reportedByDid: String,
    val createdAt: String, // TODO: datetime
    val reason: String? = null
)
