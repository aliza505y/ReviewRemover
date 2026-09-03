package com.example.reviewremover

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ReportHistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: AuditAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_history)

        rvHistory = findViewById(R.id.rvHistory)
        adapter = AuditAdapter(emptyList())
        rvHistory.adapter = adapter

        database = AppDatabase.getDatabase(this)

        // Room DB live update collection
        lifecycleScope.launch {
            database.auditDao().getAllAudits().collect { auditList ->
                adapter.updateData(auditList)
            }
        }
    }
}