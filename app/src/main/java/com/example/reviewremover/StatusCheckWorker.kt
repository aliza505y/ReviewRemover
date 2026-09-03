package com.example.reviewremover

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.net.HttpURLConnection
import java.net.URL

class StatusCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val pendingReviews = database.auditDao().getPendingAudits()

        for (item in pendingReviews) {
            val isDeleted = checkIsReviewRemoved(item.reviewUrl)

            // Agar link broken/deleted milta hai (HTTP 404 ya redirection failure)
            if (isDeleted) {
                database.auditDao().updateStatus(item.extractedReviewId, "Removed")
            }
        }

        return Result.success()
    }

    private fun checkIsReviewRemoved(urlString: String): Boolean {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            // HTTP 404 Not Found ya 410 Gone ka matlab review Google Maps se remove ho chuka hai
            responseCode == HttpURLConnection.HTTP_NOT_FOUND || responseCode == HttpURLConnection.HTTP_GONE
        } catch (e: Exception) {
            false
        }
    }
}