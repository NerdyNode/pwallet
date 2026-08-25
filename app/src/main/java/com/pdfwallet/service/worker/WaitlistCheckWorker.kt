package com.pdfwallet.service.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pdfwallet.data.db.BookingStatus
import com.pdfwallet.data.db.DocumentDao
import com.pdfwallet.util.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WaitlistCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val documentDao: DocumentDao,
    private val logger: Logger
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        logger.i("WaitlistCheckWorker", "Running periodic waitlist check...")
        
        try {
            val docs = documentDao.getAllCompleteDocuments()
            val waitlisted = docs.filter { it.bookingStatus == BookingStatus.WAITLIST }
            
            if (waitlisted.isNotEmpty()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            applicationContext,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        return Result.success()  // Can't post notifications without permission on Android 13+
                    }
                }
                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                
                waitlisted.forEach { doc ->
                    val title = "Waitlist Reminder"
                    val content = "Don't forget to check your PNR ${doc.documentId ?: ""} for ${doc.title}!"
                    
                    val builder = NotificationCompat.Builder(applicationContext, "wallet_processing")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        
                    notificationManager.notify(doc.id.toInt(), builder.build())
                }
            }
            
            return Result.success()
        } catch (e: Exception) {
            logger.e("WaitlistCheckWorker", "Failed to check waitlist", e)
            return Result.retry()
        }
    }
}
