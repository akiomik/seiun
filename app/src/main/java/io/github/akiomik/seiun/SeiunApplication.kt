package io.github.akiomik.seiun

import android.app.Application
import com.slack.eithernet.ApiResultCallAdapterFactory
import com.slack.eithernet.ApiResultConverterFactory
import io.github.akiomik.seiun.service.AtpService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class SeiunApplication: Application() {
    lateinit var userRepository: UserRepository
    lateinit var timelineRepository: TimelineRepository

    companion object {
        @get:Synchronized var instance: SeiunApplication? = null
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
        instance = this
    }
}