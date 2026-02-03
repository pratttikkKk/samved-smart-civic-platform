package com.example.samved.api

data class FeedbackRequest(
    val complaintId: String,
    val rating: Int,
    val comment: String
)