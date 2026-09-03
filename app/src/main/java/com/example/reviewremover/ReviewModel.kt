package com.example.reviewremover

data class ReportPayload(
    val reason: String = "SPAM_OR_POLICY_VIOLATION",
    val comments: String = "Automated policy compliance report."
)

data class ReportResponse(
    val name: String?,
    val state: String? // e.g., "PENDING_REVIEW"
)