package io.github.akiomik.seiun.model.type

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Blob(
    val ref: Ref,
    val mimeType: String,
    val size: Int,
    @Json(name = "\$type")
    val type: String? = "blob"
)
