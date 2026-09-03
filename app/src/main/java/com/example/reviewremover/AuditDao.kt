package com.example.reviewremover

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: AuditEntity): Long

    @Query("SELECT * FROM audit_table ORDER BY timestamp DESC")
    fun getAllAudits(): Flow<List<AuditEntity>>

    @Query("SELECT * FROM audit_table WHERE status = 'Submitted' OR status = 'PENDING_REVIEW'")
    suspend fun getPendingAudits(): List<AuditEntity>

    // Explicit Unit type define karein ya Int return type dein
    @Query("UPDATE audit_table SET status = :newStatus WHERE extractedReviewId = :reviewId")
    suspend fun updateStatus(reviewId: String, newStatus: String): Unit
}