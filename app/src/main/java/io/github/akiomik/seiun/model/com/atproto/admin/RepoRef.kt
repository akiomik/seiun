package io.github.akiomik.seiun.model.com.atproto.admin

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.HasNsid

@JsonClass(generateAdapter = true)
data class RepoRef(
    val did: String
) : HasNsid by Companion {
    @Keep
    companion object : HasNsid {
        override val nsid: String
            get() = "com.atproto.admin.defs#repoRef"
    }
}
