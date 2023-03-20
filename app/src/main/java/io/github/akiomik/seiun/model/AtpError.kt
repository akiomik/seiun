package io.github.akiomik.seiun.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AtpError(val error: String?, val message: String?)
