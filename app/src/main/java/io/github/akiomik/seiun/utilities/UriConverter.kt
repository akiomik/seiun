package io.github.akiomik.seiun.utilities

object UriConverter {
    fun toRkey(uri: String): String = uri.split('/').last()
}
