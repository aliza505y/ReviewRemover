package com.example.reviewremover

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_table")
data class AuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reviewUrl: String,
    val extractedReviewId: String,
    val status: String, // e.g., "SUBMITTED", "PENDING_MODERATION", "REMOVED"
    val timestamp: Long = System.currentTimeMillis()
)