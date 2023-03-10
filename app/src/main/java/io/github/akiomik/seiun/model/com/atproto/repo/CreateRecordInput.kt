package io.github.akiomik.seiun.model.com.atproto.repo

data class CreateRecordInput<A>(
    val did: String,
    val record: A,
    val collection: String,
    val validate: Boolean? = null,
)