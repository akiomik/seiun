package io.github.akiomik.seiun.api

import okhttp3.ResponseBody
import retrofit2.Converter

object UnitConverter : Converter<ResponseBody, Unit> {
    override fun convert(value: ResponseBody): Unit = Unit
}
