package io.github.akiomik.seiun.model.app.bsky.embed

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.HasNsid

@JsonClass(generateAdapter = true)
data class ImagesView(
    val images: List<ImagesViewImage>
) : HasNsid by Companion {
    companion object : HasNsid {
        override val nsid: String
            get() = "app.bsky.embed.images#view"
    }
}
