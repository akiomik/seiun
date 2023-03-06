package io.github.akiomik.seiun.model

data class DeleteRecordParam(
    val did: String,
    val collection: String,
    val rkey: String
)