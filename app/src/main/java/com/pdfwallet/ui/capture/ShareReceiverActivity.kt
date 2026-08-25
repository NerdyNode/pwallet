package com.pdfwallet.ui.capture

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.pdfwallet.data.db.CaptureSource
import com.pdfwallet.data.repository.DocumentRepository
import com.pdfwallet.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {

    @Inject
    lateinit var repository: DocumentRepository

    @Inject
    lateinit var logger: Logger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            var successCount = 0
            var failCount = 0

            when (intent?.action) {
                Intent.ACTION_SEND -> {
                    if ("application/pdf" == intent.type) {
                        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(Intent.EXTRA_STREAM)
                        }
                        uri?.let { if (handlePdf(it)) successCount++ else failCount++ }
                    }
                }
                Intent.ACTION_SEND_MULTIPLE -> {
                    if ("application/pdf" == intent.type) {
                        val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                        }
                        uris?.forEach { if (handlePdf(it)) successCount++ else failCount++ }
                    }
                }
            }

            val msg = when {
                successCount > 0 && failCount == 0 -> "Added to wallet — processing..."
                successCount > 0 && failCount > 0 -> "Added $successCount to wallet, $failCount failed"
                successCount == 0 && failCount > 0 -> "Failed to add to wallet"
                else -> null
            }
            
            msg?.let {
                Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    private suspend fun handlePdf(uri: Uri): Boolean {
        return try {
            repository.captureDocument(uri, CaptureSource.SHARE_INTENT)
            true
        } catch (e: Exception) {
            logger.e("ShareReceiver", "Failed to capture PDF", e)
            false
        }
    }
}
