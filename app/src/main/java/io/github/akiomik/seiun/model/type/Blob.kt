package io.github.akiomik.seiun.model.type

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// NOTE: The old blob formats don't have ref and size
@JsonClass(generateAdapter = true)
data class Blob(
    val ref: Ref? = null,
    val mimeType: String,
    val size: Int? = null,
    val original: BlobOriginal? = null,
    @Json(name = "\$type")
    val type: String? = "blob"
)
