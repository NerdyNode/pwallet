package com.pdfwallet

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

@HiltAndroidApp
class PdfWalletApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncManager: com.pdfwallet.service.sync.SyncManager

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        val appCheckProviderFactory = if (BuildConfig.DEBUG) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            appCheckProviderFactory
        )
        try {
            com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(applicationContext)
        } catch (e: Exception) {
            // Log but don't crash — PDF text extraction will fall back to OCR
            android.util.Log.e("PdfWalletApp", "Failed to initialize PDFBox", e)
        }
        createNotificationChannels()
        scheduleWaitlistWorker()
        scheduleExpiryWorker()
        syncManager.schedulePeriodicSync()
    }

    private fun scheduleExpiryWorker() {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.pdfwallet.service.worker.ExpiryCheckWorker>(
            24, java.util.concurrent.TimeUnit.HOURS
        ).build()
        androidx.work.WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "expiry_check",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleWaitlistWorker() {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.pdfwallet.service.worker.WaitlistCheckWorker>(
            1, java.util.concurrent.TimeUnit.HOURS
        ).build()
        androidx.work.WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "waitlist_check",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun createNotificationChannels() {
        val channel = android.app.NotificationChannel(
            "wallet_processing",
            "Document Processing",
            android.app.NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for imported documents"
        }
        val notificationManager = getSystemService(android.app.NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        
        val processingChannel = android.app.NotificationChannel(
            "wallet_processing",
            "Document Processing",
            android.app.NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows progress while documents are being processed"
        }
        notificationManager.createNotificationChannel(processingChannel)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
