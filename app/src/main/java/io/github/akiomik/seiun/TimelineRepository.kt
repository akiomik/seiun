package io.github.akiomik.seiun

import android.util.Log
import io.github.akiomik.seiun.service.AtpService
import io.github.akiomik.seiun.model.Session
import io.github.akiomik.seiun.model.Timeline
import io.github.akiomik.seiun.service.UnauthorizedException

class TimelineRepository(private val atpService: AtpService) {
    fun getTimeline(session: Session): Timeline {
        Log.d("Seiun", "Get timeline")

        val res = atpService.getTimeline("Bearer ${session.accessJwt}").execute()
        if (res.code() == 401) {
            throw UnauthorizedException("Empty body on getTimeline")
        }

        return res.body() ?: throw IllegalStateException("Empty body on getTimeline")
    }
}