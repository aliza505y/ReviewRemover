package com.example.reviewremover

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ReviewApiService {
    @POST("v1/accounts/{accountId}/locations/{locationId}/reviews/{reviewId}:report")
    suspend fun submitReportToGoogle(
        @Header("Authorization") authHeader: String,
        @Path("accountId") accountId: String,
        @Path("locationId") locationId: String,
        @Path("reviewId") reviewId: String,
        @Body payload: ReportPayload
    ): Response<ReportResponse>
}