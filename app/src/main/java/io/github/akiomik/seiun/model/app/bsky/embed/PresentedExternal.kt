package io.github.akiomik.seiun.model.app.bsky.embed

data class PresentedExternal(
    val uri: String,
    val title: String,
    val description: String,
    val thumb: String? = null
)
