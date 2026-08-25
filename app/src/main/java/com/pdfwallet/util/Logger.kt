package com.pdfwallet.util

import android.util.Log
import com.pdfwallet.data.db.AppLog
import com.pdfwallet.data.db.AppLogDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.pdfwallet.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class Logger @Inject constructor(
    private val appLogDao: AppLogDao,
    @ApplicationScope private val scope: CoroutineScope
) {

    open fun i(tag: String, message: String) {
        Log.i(tag, message)
        saveLog(tag, message, "INFO")
    }

    open fun d(tag: String, message: String) {
        Log.d(tag, message)
        saveLog(tag, message, "DEBUG")
    }

    open fun w(tag: String, message: String) {
        Log.w(tag, message)
        saveLog(tag, message, "WARN")
    }

    open fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        val stackTrace = throwable?.stackTraceToString()
        saveLog(tag, message, "ERROR", stackTrace)
    }

    private fun saveLog(tag: String, message: String, severity: String, exceptionTrace: String? = null) {
        scope.launch {
            try {
                appLogDao.insert(
                    AppLog(
                        tag = tag,
                        message = message,
                        severity = severity,
                        exceptionTrace = exceptionTrace
                    )
                )
            } catch (e: Exception) {
                // Fallback if DB insert fails
                Log.e("Logger", "Failed to save log to DB", e)
            }
        }
    }
}
