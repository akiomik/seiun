package io.github.akiomik.seiun.model.com.atproto.repo

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.Blob

@JsonClass(generateAdapter = true)
data class UploadBlobOutput(val blob: Blob)
