package io.github.akiomik.seiun.model.com.atproto.repo

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteRecordInput(
    val repo: String,
    val collection: String,
    val rkey: String,
    val swapRecord: String? = null,
    val swapCommit: String? = null
)
