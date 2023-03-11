package io.github.akiomik.seiun.model

interface ISession {
    val accessJwt: String
    val refreshJwt: String
    val handle: String
    val did: String
}
