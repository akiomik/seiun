package io.github.akiomik.seiun

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.akiomik.seiun.api.AtpService
import io.github.akiomik.seiun.datastores.CredentialDataStore
import io.github.akiomik.seiun.datastores.PreferencesDataStore
import io.github.akiomik.seiun.datastores.SessionDataStore
import io.github.akiomik.seiun.repository.AuthRepository
import io.github.akiomik.seiun.repository.NotificationRepository
import io.github.akiomik.seiun.repository.PostFeedRepository
import io.github.akiomik.seiun.repository.PreferencesRepository
import io.github.akiomik.seiun.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Duration

class SeiunApplication : Application() {
    private var _atpService = MutableStateFlow<AtpService?>(null)
    val atpService = _atpService as StateFlow<AtpService?>

    private lateinit var credentialDataStore: CredentialDataStore
    private lateinit var sessionDataStore: SessionDataStore
    private lateinit var preferencesDataStore: PreferencesDataStore
    lateinit var authRepository: AuthRepository
    lateinit var userRepository: UserRepository
    lateinit var notificationRepository: NotificationRepository
    lateinit var postFeedRepository: PostFeedRepository
    lateinit var preferencesRepository: PreferencesRepository

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
        preferencesDataStore = PreferencesDataStore(applicationContext)
        authRepository = AuthRepository(credentialDataStore, sessionDataStore)
        userRepository = UserRepository(authRepository)
        notificationRepository = NotificationRepository(authRepository)
        postFeedRepository = PostFeedRepository(authRepository)
        preferencesRepository = PreferencesRepository(preferencesDataStore)
        instance = this
    }

    fun setAtpClient() {
        setAtpClient(credentialDataStore.get().serviceProvider)
    }

    fun setAtpClient(serviceProvider: String) {
        Log.d(TAG, "Change serviceProvider to $serviceProvider")

        _atpService.value = AtpService.create(serviceProvider)
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
