package io.github.akiomik.seiun.model.com.atproto.repo

data class DeleteRecordInput(
    val did: String,
    val collection: String,
    val rkey: String
)
