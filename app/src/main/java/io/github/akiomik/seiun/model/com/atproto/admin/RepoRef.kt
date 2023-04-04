package io.github.akiomik.seiun.model.com.atproto.admin

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.HasNsid

@JsonClass(generateAdapter = true)
data class RepoRef(
    val did: String
) : HasNsid by Companion {
    companion object : HasNsid {
        override val nsid: String
            get() = "com.atproto.admin.defs#repoRef"
    }
}
