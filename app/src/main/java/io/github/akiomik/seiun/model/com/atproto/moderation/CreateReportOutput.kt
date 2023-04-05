package io.github.akiomik.seiun.model.com.atproto.moderation

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.com.atproto.admin.RepoRef
import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef
import io.github.akiomik.seiun.model.type.Union2
import java.util.*

@JsonClass(generateAdapter = true)
data class CreateReportOutput(
    val id: Int,
    val reasonType: ReasonType,
    val subject: Union2<RepoRef, StrongRef>,
    val reportedBy: String,
    val createdAt: Date,
    val reason: String? = null
)
