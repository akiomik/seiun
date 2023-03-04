package io.github.akiomik.seiun

import android.app.Application
import com.example.catpaw.services.AtpService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class SeiunApplication: Application() {
    lateinit var userRepository: UserRepository
    lateinit var atpService: AtpService

    companion object {
        @get:Synchronized var instance: SeiunApplication? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://bsky.social/xrpc/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        atpService = retrofit.create(AtpService::class.java)
        userRepository = UserRepository(applicationContext)
        instance = this
    }
}