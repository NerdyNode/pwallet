package com.pdfwallet.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: Document): Long

    @Query("SELECT * FROM documents ORDER BY importDate DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Query("SELECT * FROM documents ORDER BY importDate DESC")
    fun getPagedDocuments(): PagingSource<Int, Document>

    @Query("SELECT * FROM documents WHERE documentType = :type ORDER BY importDate DESC")
    fun getByType(type: DocumentType): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE (:typeFilterSize = 0 OR documentType IN (:types)) AND (title LIKE :q OR holderName LIKE :q OR documentId LIKE :q) ORDER BY " +
           "CASE WHEN :sort = 'DATE_ADDED' THEN importDate END DESC, " +
           "CASE WHEN :sort = 'NAME_ASC' THEN holderName END ASC, " +
           "CASE WHEN :sort = 'NAME_DESC' THEN holderName END DESC")
    fun searchDocuments(q: String, types: List<DocumentType>, typeFilterSize: Int, sort: String): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE processingStatus = 'COMPLETE'")
    suspend fun getAllCompleteDocuments(): List<Document>

    @Delete
    suspend fun delete(document: Document)

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: Long): Document?

    @Query("SELECT * FROM documents WHERE contentHash = :hash LIMIT 1")
    suspend fun getByContentHash(hash: String): Document?

    @Query("UPDATE documents SET processingStatus = :status WHERE id = :id")
    suspend fun updateProcessingStatus(id: Long, status: ProcessingStatus)
    
    @Update
    suspend fun update(document: Document)
    
    @Query("SELECT documentType, COUNT(*) as count FROM documents GROUP BY documentType")
    fun getDocumentTypeCounts(): Flow<List<DocumentTypeCount>>

    @Query("SELECT * FROM documents WHERE expiryDateEpoch IS NOT NULL AND expiryDateEpoch <= :epochMillis AND processingStatus = 'COMPLETE'")
    fun getDocumentsExpiringBefore(epochMillis: Long): Flow<List<Document>>

    @Query("SELECT holderName FROM documents WHERE holderName IS NOT NULL AND holderName != '' GROUP BY holderName ORDER BY COUNT(holderName) DESC LIMIT 1")
    fun getMostFrequentHolderName(): Flow<String?>
}
