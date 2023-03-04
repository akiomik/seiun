package io.github.akiomik.seiun

import android.util.Log
import com.example.catpaw.services.AtpService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.akiomik.seiun.model.Session
import io.github.akiomik.seiun.model.Timeline
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class TimelineRepository(private val atpService: AtpService) {
    fun getTimeline(session: Session): Timeline {
        Log.d("Seiun", "getTimeliine")
        // TODO: improve error handling
        return atpService.getTimeline("Bearer ${session.accessJwt}").execute().body()
            ?: throw IllegalStateException("Empty body on getTimeline")
    }
}