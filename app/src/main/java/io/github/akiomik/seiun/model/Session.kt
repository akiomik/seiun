package io.github.akiomik.seiun.model

data class Session(val accessJwt: String, val refreshJwt: String, val handle: String, val did: String)