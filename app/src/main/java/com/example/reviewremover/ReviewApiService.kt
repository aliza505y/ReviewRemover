package com.example.reviewremover

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface ReviewApiService {

    @GET
    suspend fun getAllReviews(
        @Url fullUrl: String,
        @Header("Authorization") authToken: String
    ): Response<List<ReviewModel>>

    @DELETE
    suspend fun deleteReviewById(
        @Url deleteEndpointUrl: String,
        @Header("Authorization") authToken: String
    ): Response<Unit>
}