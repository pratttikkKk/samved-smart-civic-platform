package com.example.samved.model

data class Complaint(
    val _id: String,
    val issueType: String,
    val description: String,
    val ward: String,
    val addressText: String,
    val latitude: Double,
    val longitude: Double,
    val beforePhoto: String,
    val afterPhoto: String?,
    val status: String,
    val priorityScore: Int,
    val reporters: List<String>

)
