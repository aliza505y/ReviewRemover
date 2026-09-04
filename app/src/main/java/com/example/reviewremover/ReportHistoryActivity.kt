package com.example.reviewremover

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class ReportHistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: AuditAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_history)

        rvHistory = findViewById(R.id.rvHistory)
        database = AppDatabase.getDatabase(this)

        // Adapter initialization with item click callback
        adapter = AuditAdapter(emptyList()) { selectedAudit ->
            verifySingleReviewStatus(selectedAudit)
        }
        rvHistory.adapter = adapter

        // Room DB live update collection
        lifecycleScope.launch {
            database.auditDao().getAllAudits().collect { auditList ->
                adapter.updateData(auditList)
            }
        }

        val btnClearHistory: Button = findViewById(R.id.btnClearHistory)

        btnClearHistory.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                database.auditDao().deleteAllAudits()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportHistoryActivity, "History Cleared!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun verifySingleReviewStatus(audit: AuditEntity) {
        Toast.makeText(this, "Checking status for review...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
            val isRemoved = checkIsReviewRemoved(audit.reviewUrl)

            if (isRemoved) {
                database.auditDao().updateStatus(audit.extractedReviewId, "Removed")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportHistoryActivity, "Status Updated: Removed!", Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportHistoryActivity, "Review is still active", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkIsReviewRemoved(urlString: String): Boolean {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            )
            connection.instanceFollowRedirects = true
            connection.connect()

            val responseCode = connection.responseCode
            responseCode == HttpURLConnection.HTTP_NOT_FOUND || responseCode == HttpURLConnection.HTTP_GONE
        } catch (e: Exception) {
            false
        }
    }
}