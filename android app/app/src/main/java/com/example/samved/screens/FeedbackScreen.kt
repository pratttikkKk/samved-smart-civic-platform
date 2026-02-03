package com.example.samved.screens

import com.example.samved.api.FeedbackRequest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.samved.api.ApiClient
import kotlinx.coroutines.launch

@Composable
fun FeedbackScreen(complaintId: String) {

    var rating by remember { mutableStateOf(3f) }
    var comment by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "Service Feedback",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        Text("Rate the resolution quality")

        Slider(
            value = rating,
            onValueChange = { rating = it },
            valueRange = 1f..5f,
            steps = 3
        )

        Row {
            repeat(rating.toInt()) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Your feedback") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        loading = true
                        message = ""

                        val res = ApiClient.api.sendFeedback(
                            FeedbackRequest(
                                complaintId = complaintId,
                                rating = rating.toInt(),
                                comment = comment
                            )
                        )

                        if (res.isSuccessful) {
                            message = "✅ Thank you! Feedback submitted."
                        } else {
                            message = "❌ Failed: ${res.code()}"
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        message = "❌ Error: ${e.localizedMessage ?: "Unknown error"}"
                    } finally {
                        loading = false
                    }
                }
            }
            ,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            enabled = !loading
        ) {
            Text(if (loading) "Submitting..." else "Submit Feedback")
        }

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                message,
                color = if (message.startsWith("✅")) Color(0xFF2E7D32) else Color.Red
            )
        }
    }
}
