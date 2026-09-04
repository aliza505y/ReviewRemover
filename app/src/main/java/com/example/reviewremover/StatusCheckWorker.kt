package com.example.reviewremover

import android.content.Context
import android.util.Log
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

            Log.d("StatusWorker", "Checking ID: ${item.extractedReviewId} | Is Deleted: $isDeleted")

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

            // HEAD ki jagah GET use karein
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            // Browser header imitate karein taake Google block na kare
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            )
            // Redirects allow karein short links ke liye
            connection.instanceFollowRedirects = true

            connection.connect()
            val responseCode = connection.responseCode

            Log.d("StatusWorker", "URL: $urlString -> Response Code: $responseCode")

            // 404, 410 ya bad redirect check
            responseCode == HttpURLConnection.HTTP_NOT_FOUND ||
                    responseCode == HttpURLConnection.HTTP_GONE

        } catch (e: Exception) {
            Log.e("StatusWorker", "Network Error checking URL: ${e.message}")
            false
        }
    }
}