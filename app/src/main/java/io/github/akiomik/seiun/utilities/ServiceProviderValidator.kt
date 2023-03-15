package io.github.akiomik.seiun.utilities

import android.net.Uri

object ServiceProviderValidator {
    fun validate(serviceProvider: String): Boolean {
        if (serviceProvider.split(".").size < 2) {
            return false
        }

        return try {
            Uri.parse("https://$serviceProvider/xrpc")
            true
        } catch (e: Throwable) {
            false
        }
    }
}
