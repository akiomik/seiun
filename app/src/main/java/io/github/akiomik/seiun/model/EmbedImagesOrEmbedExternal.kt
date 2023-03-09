package io.github.akiomik.seiun.model

import com.squareup.moshi.Json

data class EmbedImagesOrEmbedExternal(
    @Json(name = "\$type")
    val type: String,
    val images: List<Image>?
    // TODO val external: External?
)
