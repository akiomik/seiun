package io.github.akiomik.seiun

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.slack.eithernet.ApiResultCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.akiomik.seiun.repository.NotificationRepository
import io.github.akiomik.seiun.repository.TimelineRepository
import io.github.akiomik.seiun.repository.UserRepository
import io.github.akiomik.seiun.service.AtpService
import io.github.akiomik.seiun.service.CustomApiResultConverterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.time.Duration
import java.util.*

class SeiunApplication : Application() {
    var atpService: AtpService? = null
    lateinit var userRepository: UserRepository
    lateinit var timelineRepository: TimelineRepository
    lateinit var notificationRepository: NotificationRepository

    companion object {
        @get:Synchronized
        var instance: SeiunApplication? = null
            private set

        const val TAG = "Seiun"
        const val NOTIFICATION_ID = 0
    }

    override fun onCreate() {
        super.onCreate()

        userRepository = UserRepository(applicationContext)
        timelineRepository = TimelineRepository()
        notificationRepository = NotificationRepository()
        instance = this
    }

    fun isAtpServiceInitialized(): Boolean {
        return atpService != null
    }

    fun setAtpClient() {
        setAtpClient(userRepository.getLoginParam().first)
    }

    fun setAtpClient(serviceProvider: String) {
        Log.d(TAG, "Change serviceProvider to $serviceProvider")

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .build()
        atpService = Retrofit.Builder()
            .baseUrl("https://$serviceProvider/xrpc/")
            .addConverterFactory(CustomApiResultConverterFactory)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(ApiResultCallAdapterFactory)
            .build()
            .create()
    }

    fun registerNotificationWorker() {
        val notificationWorkRequest =
            PeriodicWorkRequestBuilder<NotificationWorker>(Duration.ofMinutes(15))
                .setInitialDelay(Duration.ofMinutes(3))
                .build()
        val manager = WorkManager.getInstance(this)
        manager.cancelAllWork()
        manager.enqueue(notificationWorkRequest)
    }

    fun clearNotifications() {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
