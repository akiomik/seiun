package io.github.akiomik.seiun.repository

import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.api.AtpService

abstract class ApplicationRepository {
    protected fun getAtpClient(): AtpService {
        return SeiunApplication.instance!!.atpService.value!!
    }
}
