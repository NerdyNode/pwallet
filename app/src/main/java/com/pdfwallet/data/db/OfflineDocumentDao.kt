package com.pdfwallet.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OfflineDocumentDao {
    @Insert
    suspend fun insert(document: OfflineDocumentEntity)

    @Query("SELECT * FROM offline_documents ORDER BY addedAt ASC")
    suspend fun getAllPending(): List<OfflineDocumentEntity>

    @Query("DELETE FROM offline_documents WHERE id = :id")
    suspend fun delete(id: Long)
}
