package io.github.akiomik.seiun.datastores

data class Credential(
    val serviceProvider: String,
    val handleOrEmail: String,
    val password: String
)
