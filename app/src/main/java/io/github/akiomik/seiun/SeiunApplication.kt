package io.github.akiomik.seiun

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.akiomik.seiun.api.AtpService
import io.github.akiomik.seiun.datastores.CredentialDataStore
import io.github.akiomik.seiun.datastores.SessionDataStore
import io.github.akiomik.seiun.repository.AuthRepository
import io.github.akiomik.seiun.repository.NotificationRepository
import io.github.akiomik.seiun.repository.TimelineRepository
import io.github.akiomik.seiun.repository.UserRepository
import java.time.Duration

class SeiunApplication : Application() {
    var atpService: AtpService? = null
    private lateinit var credentialDataStore: CredentialDataStore
    private lateinit var sessionDataStore: SessionDataStore
    lateinit var authRepository: AuthRepository
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

        credentialDataStore = CredentialDataStore(applicationContext)
        sessionDataStore = SessionDataStore(applicationContext)
        authRepository = AuthRepository(credentialDataStore, sessionDataStore)
        userRepository = UserRepository(authRepository)
        timelineRepository = TimelineRepository(authRepository)
        notificationRepository = NotificationRepository(authRepository)
        instance = this
    }

    fun isAtpServiceInitialized(): Boolean {
        return atpService != null
    }

    fun setAtpClient() {
        setAtpClient(credentialDataStore.get().serviceProvider)
    }

    fun setAtpClient(serviceProvider: String) {
        Log.d(TAG, "Change serviceProvider to $serviceProvider")

        atpService = AtpService.create(serviceProvider)
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
