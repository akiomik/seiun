package io.github.akiomik.seiun.utilities

import android.icu.text.CompactDecimalFormat
import java.util.*

object NumberFormatter {
    fun compact(number: Int): String {
        val locale = Locale.getDefault()
        val compactDecimalFormat: CompactDecimalFormat =
            CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT)
        return compactDecimalFormat.format(number)
    }
}
