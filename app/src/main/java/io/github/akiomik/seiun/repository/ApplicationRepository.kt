package io.github.akiomik.seiun.repository

import com.slack.eithernet.ApiResult
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.api.AtpService
import io.github.akiomik.seiun.api.RequestHelper
import io.github.akiomik.seiun.model.AtpError

abstract class ApplicationRepository {
    protected fun getAtpClient(): AtpService {
        return SeiunApplication.instance!!.atpService!!
    }

    protected suspend fun <A : Any> handleRequest(run: suspend () -> ApiResult<A, AtpError>): A {
        return RequestHelper.execute(run)
    }
}
