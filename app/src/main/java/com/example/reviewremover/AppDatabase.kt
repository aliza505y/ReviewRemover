package com.example.reviewremover

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AuditEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun auditDao(): AuditDao
}