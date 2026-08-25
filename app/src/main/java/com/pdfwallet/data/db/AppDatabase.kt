package com.pdfwallet.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Document::class, AppLog::class, OfflineDocumentEntity::class], version = 6, exportSchema = false)
@TypeConverters(MetadataConverter::class, EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun appLogDao(): AppLogDao
    abstract fun offlineDocumentDao(): OfflineDocumentDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documents ADD COLUMN bookingStatus TEXT")
                db.execSQL("ALTER TABLE documents ADD COLUMN journeyDate INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documents ADD COLUMN expiryDateEpoch INTEGER")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `offline_documents` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`filePath` TEXT NOT NULL, " +
                            "`addedAt` INTEGER NOT NULL, " +
                            "`retryCount` INTEGER NOT NULL)"
                )
            }
        }
        
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documents ADD COLUMN isSensitive INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE documents ADD COLUMN maskedIdentifier TEXT")
                db.execSQL("ALTER TABLE documents ADD COLUMN collectionId INTEGER")
                db.execSQL("ALTER TABLE documents ADD COLUMN localPath TEXT")
            }
        }
    }
}
