package com.pdfwallet.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_logs")
data class AppLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val severity: String = "INFO", // INFO, ERROR, WARN, DEBUG
    val exceptionTrace: String? = null
)
