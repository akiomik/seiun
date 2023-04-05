package io.github.akiomik.seiun.model.type

sealed interface Union2<out A, out B> {
    data class Element1<out A, out B>(val value: A) : Union2<A, B>
    data class Element2<out A, out B>(val value: B) : Union2<A, B>
}
