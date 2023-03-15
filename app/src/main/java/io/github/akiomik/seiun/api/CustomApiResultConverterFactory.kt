package io.github.akiomik.seiun.api

import com.slack.eithernet.ApiResult
import com.slack.eithernet.ApiResultConverterFactory
import com.squareup.moshi.Types
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

object CustomApiResultConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (Types.getRawType(type) == ApiResult::class.java) {
            return ApiResultConverterFactory.responseBodyConverter(
                type,
                annotations,
                retrofit
            )
        }

        if (Types.getRawType(type) == Unit::class.java) {
            return UnitConverter
        }

        return null
    }
}
