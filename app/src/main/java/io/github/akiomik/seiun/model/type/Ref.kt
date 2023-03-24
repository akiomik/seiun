package io.github.akiomik.seiun.model.type

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Ref(
    @Json(name = "\$link")
    val link: String
)
