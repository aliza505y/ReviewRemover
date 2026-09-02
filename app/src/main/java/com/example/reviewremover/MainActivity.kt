package com.example.reviewremover

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.reviewremover.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var apiService: ReviewApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.green)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false

        binding.tvLogs.movementMethod = ScrollingMovementMethod()

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "review_audit_db")
            .fallbackToDestructiveMigration()
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://placeholder.local/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ReviewApiService::class.java)

        binding.btnStartCleaning.setOnClickListener {
            startDatabaseCleaningProcess()
        }
    }

    private fun startDatabaseCleaningProcess() {
        val baseUrl = binding.etBaseUrl.text.toString().trim()
        val apiKey = binding.etApiKey.text.toString().trim()

        if (baseUrl.isEmpty() || apiKey.isEmpty()) {
            appendLog("\n>> [ERROR] Please enter Client Base URL and Admin API Key.")
            return
        }

        binding.btnStartCleaning.isEnabled = false
        binding.tvLogs.text = "================== [SERVER DB CLEANUP STARTED] =================="

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    appendLog("\n>> Connecting to Client Server Endpoint...")
                    binding.progressBar.progress = 20
                }
                delay(500)

                val fetchUrl = if (baseUrl.endsWith("/")) "${baseUrl}api/reviews" else "$baseUrl/api/reviews"
                val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"

                withContext(Dispatchers.Main) {
                    appendLog("\n>> Pulling live database records from: $fetchUrl")
                }

                val response = apiService.getAllReviews(fetchUrl, authHeader)

                if (response.isSuccessful && response.body() != null) {
                    val allReviews = response.body()!!
                    val totalCount = allReviews.size

                    withContext(Dispatchers.Main) {
                        appendLog("\n>> [PULL SUCCESS] Total Reviews Fetched: $totalCount")
                        binding.progressBar.progress = 50
                    }
                    delay(400)

                    val badReviews = allReviews.filter { it.rating <= 2 }
                    val goodReviewsCount = totalCount - badReviews.size

                    withContext(Dispatchers.Main) {
                        appendLog("\n>> [FILTER ANALYSIS] Bad Reviews Flagged (<= 2 Stars): ${badReviews.size}")
                        appendLog("\n>> [RETAIN] Positive Reviews Preserved (3-5 Stars): $goodReviewsCount")
                    }

                    if (badReviews.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            appendLog("\n>> [INFO] Server database is clean. No action required.")
                        }
                    } else {
                        badReviews.forEachIndexed { index, review ->
                            val currentStep = index + 1
                            withContext(Dispatchers.Main) {
                                appendLog("\n\n----------------- [REMOVING BAD REVIEW $currentStep/${badReviews.size}] -----------------")
                                appendLog("\n>> Target ID: ${review.id} | Author: ${review.reviewerName}")
                                appendLog("\n>> Rating: ${review.rating} Stars | Content: \"${review.reviewText}\"")
                            }

                            val deleteUrl = "$fetchUrl/${review.id}"
                            val deleteResponse = apiService.deleteReviewById(deleteUrl, authHeader)

                            if (deleteResponse.isSuccessful) {
                                val caseId = "CASE-" + UUID.randomUUID().toString().substring(0, 5).uppercase(Locale.ROOT)
                                val timeFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                                val auditEntry = AuditEntity(
                                    caseId = caseId,
                                    reviewId = review.id,
                                    actionTaken = "DELETED_RATING_${review.rating}_STAR",
                                    timestamp = timeFormatted
                                )

                                db.auditDao().insertAudit(auditEntry)

                                withContext(Dispatchers.Main) {
                                    appendLog("\n>> [SERVER ACTION] Deleted successfully from backend DB.")
                                    appendLog("\n>> [ROOM DB AUDIT] Logged under Case ID: $caseId")
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    appendLog("\n>> [FAILED] Server returned status code: ${deleteResponse.code()}")
                                }
                            }
                            delay(600)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        binding.progressBar.progress = 100
                        appendLog("\n\n================== [CLEANUP PROCESS COMPLETED] ==================")
                        appendLog("\n>> Server state synced. Only positive reviews are active on backend.")
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        appendLog("\n>> [ERROR] Fetch failed. Server response code: ${response.code()}")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    appendLog("\n>> [EXCEPTION] Network/Database Failure: ${e.localizedMessage}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.btnStartCleaning.isEnabled = true
                }
            }
        }
    }

    private fun appendLog(message: String) {
        binding.tvLogs.append(message)
        binding.tvLogs.post {
            val scrollAmount = binding.tvLogs.layout?.getLineTop(binding.tvLogs.lineCount) ?: 0
            if (scrollAmount > binding.tvLogs.height) {
                binding.tvLogs.scrollTo(0, scrollAmount - binding.tvLogs.height)
            }
        }
    }
}