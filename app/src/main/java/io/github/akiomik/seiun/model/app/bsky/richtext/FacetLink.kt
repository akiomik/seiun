package io.github.akiomik.seiun.model.app.bsky.richtext

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.HasNsid

@JsonClass(generateAdapter = true)
data class FacetLink(
    val uri: String
) : HasNsid by Companion {
    @Keep
    companion object : HasNsid {
        override val nsid: String
            get() = "app.bsky.richtext.facet#link"
    }
}
