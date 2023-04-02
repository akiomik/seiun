package io.github.akiomik.seiun.model.com.atproto.repo

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateRecordInput<A>(
    val repo: String,
    val record: A,
    val collection: String,
    val rkey: String? = null,
    val validate: Boolean? = null,
    val swapCommit: String? = null
)
