package io.github.akiomik.seiun

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catpaw.models.Session
import com.example.catpaw.models.Timeline
import com.example.catpaw.services.AtpService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.concurrent.thread

class TimelineViewModel : ViewModel() {
    sealed class State {
        object Loading: State()
        data class Data(val timeline: Timeline): State()
    }

    private var _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://bsky.social/xrpc/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val userRepository = SeiunApplication.instance!!.userRepository

    init {
        viewModelScope.launch {
            while (isActive) {
                // TODO: improve thread handling
                thread {
                    val session = userRepository.getSession()
                    val data = getTimeline(session)
                    _state.value = State.Data(data)
                }
                delay(10 * 1000)
            }
        }
    }

    private fun getTimeline(session: Session): Timeline {
        Log.d("Seiun", "getTimeliine")
        // TODO: improve error handling
        val service: AtpService = retrofit.create(AtpService::class.java)
        return service.getTimeline("Bearer ${session.accessJwt}").execute().body()
            ?: throw IllegalStateException("Empty body on getTimeline")
    }
}