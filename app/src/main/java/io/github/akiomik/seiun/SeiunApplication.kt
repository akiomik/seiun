package io.github.akiomik.seiun

import android.app.Application

class SeiunApplication: Application() {
    lateinit var userRepository: UserRepository

    companion object {
        @get:Synchronized var instance: SeiunApplication? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()

        userRepository = UserRepository(applicationContext)
        instance = this
    }
}