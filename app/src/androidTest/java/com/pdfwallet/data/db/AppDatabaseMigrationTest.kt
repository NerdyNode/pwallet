package com.pdfwallet.data.db

import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @Test
    fun migrate3To4() {
        // Since exportSchema is false, we can't easily use MigrationTestHelper. 
        // We will test it manually using a SupportSQLiteDatabase instance.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dbHelper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(TEST_DB)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(3) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS `documents` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `documentType` TEXT NOT NULL, `importDate` INTEGER NOT NULL, `filePath` TEXT NOT NULL, `thumbnailPath` TEXT, `rawOcrText` TEXT, `title` TEXT NOT NULL, `documentId` TEXT, `holderName` TEXT, `issueDate` TEXT, `expiryDate` TEXT, `sourceLocation` TEXT, `destinationLocation` TEXT, `additionalMeta` TEXT, `bookingStatus` TEXT, `journeyDate` INTEGER, `processingStatus` TEXT NOT NULL, `contentHash` TEXT NOT NULL, `captureSource` TEXT NOT NULL)")
                    }

                    override fun onUpgrade(
                        db: androidx.sqlite.db.SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) {
                        if (oldVersion == 3 && newVersion == 4) {
                            AppDatabase.Companion.MIGRATION_3_4.migrate(db)
                        }
                    }
                })
                .build()
        )
        
        val db = dbHelper.writableDatabase
        db.version = 4
        
        val cursor = db.query("PRAGMA table_info(documents)")
        var hasExpiryDateEpoch = false
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            if (name == "expiryDateEpoch") {
                hasExpiryDateEpoch = true
                break
            }
        }
        cursor.close()
        db.close()
        
        assertTrue("Column expiryDateEpoch should exist after migration", hasExpiryDateEpoch)
    }
}
