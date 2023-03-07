package io.github.akiomik.seiun

import android.app.Application
import com.slack.eithernet.ApiResultCallAdapterFactory
import com.slack.eithernet.ApiResultConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.akiomik.seiun.repository.NotificationRepository
import io.github.akiomik.seiun.repository.TimelineRepository
import io.github.akiomik.seiun.repository.UserRepository
import io.github.akiomik.seiun.service.AtpService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class SeiunApplication : Application() {
    lateinit var userRepository: UserRepository
    lateinit var timelineRepository: TimelineRepository
    lateinit var notificationRepository: NotificationRepository

    companion object {
        @get:Synchronized
        var instance: SeiunApplication? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val atpService = Retrofit.Builder()
            .baseUrl("https://bsky.social/xrpc/")
            .addConverterFactory(ApiResultConverterFactory)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(ApiResultCallAdapterFactory)
            .build()
            .create<AtpService>()

        userRepository = UserRepository(applicationContext, atpService)
        timelineRepository = TimelineRepository(atpService)
        notificationRepository = NotificationRepository(atpService)
        instance = this
    }
}