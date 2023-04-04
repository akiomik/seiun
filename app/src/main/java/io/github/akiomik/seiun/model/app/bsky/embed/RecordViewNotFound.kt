package io.github.akiomik.seiun.model.app.bsky.embed

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.HasNsid

@JsonClass(generateAdapter = true)
data class RecordViewNotFound(
    val uri: String
) : HasNsid by Companion {
    companion object : HasNsid {
        override val nsid: String
            get() = "app.bsky.embed.record#viewNotFound"
    }
}
