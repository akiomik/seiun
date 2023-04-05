package io.github.akiomik.seiun.model.type

sealed interface Union4<out A, out B, out C, out D> {
    data class Element1<out A, out B, out C, out D>(val value: A) : Union4<A, B, C, D>
    data class Element2<out A, out B, out C, out D>(val value: B) : Union4<A, B, C, D>
    data class Element3<out A, out B, out C, out D>(val value: C) : Union4<A, B, C, D>
    data class Element4<out A, out B, out C, out D>(val value: D) : Union4<A, B, C, D>
}
