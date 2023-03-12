package io.github.akiomik.seiun.model.app.bsky.report

import java.util.*

data class ReportCreateOutput(
    val id: Int,
    val reasonType: String,
    val subject: RepoRefOrStrongRef,
    val reportedByDid: String,
    val createdAt: Date,
    val reason: String? = null
)
