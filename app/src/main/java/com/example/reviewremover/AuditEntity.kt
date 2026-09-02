package com.example.reviewremover

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val caseId: String,
    val reviewId: String,
    val actionTaken: String,
    val timestamp: String
)