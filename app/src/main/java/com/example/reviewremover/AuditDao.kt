package com.example.reviewremover

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AuditDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAudit(audit: AuditEntity): Long

    @Query("SELECT * FROM audit_logs ORDER BY id DESC")
    fun getAllLogs(): List<AuditEntity>
}