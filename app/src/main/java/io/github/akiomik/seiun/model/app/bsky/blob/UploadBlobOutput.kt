package io.github.akiomik.seiun.model.app.bsky.blob

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadBlobOutput(val cid: String)
