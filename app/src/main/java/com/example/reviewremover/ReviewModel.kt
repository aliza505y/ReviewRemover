package com.example.reviewremover

import com.google.gson.annotations.SerializedName

data class ReviewModel(
    @SerializedName("id") val id: String,
    @SerializedName("reviewer_name") val reviewerName: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("review_text") val reviewText: String,
    @SerializedName("created_at") val createdAt: String? = null
)