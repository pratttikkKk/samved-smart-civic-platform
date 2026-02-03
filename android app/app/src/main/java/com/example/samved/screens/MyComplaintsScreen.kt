package com.example.samved.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.samved.api.ApiClient
import com.example.samved.model.Complaint
import kotlinx.coroutines.launch

@Composable
fun MyComplaintsScreen(
    userId: String,
    onGiveFeedback: (String) -> Unit
) {

    val scope = rememberCoroutineScope()
    var complaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val res = ApiClient.api.getMyComplaints(userId)
                if (res.isSuccessful) {
                    complaints = res.body() ?: emptyList()
                } else {
                    error = "Failed to load complaints (${res.code()})"
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    when {
        loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(error ?: "", color = Color.Red)
            }
        }

        complaints.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No complaints found")
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                items(complaints) { complaint ->
                    MyComplaintCard(complaint, userId, onGiveFeedback)
                }
            }
        }
    }
}

@Composable
fun MyComplaintCard(
    complaint: Complaint,
    userId: String,
    onGiveFeedback: (String) -> Unit
) {

    val imageBaseUrl = "http://10.0.2.2:5000/uploads/"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    complaint.issueType,
                    style = MaterialTheme.typography.titleMedium
                )

                AssistChip(
                    onClick = {},
                    label = { Text("Priority ${complaint.priorityScore}") }
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                "Status: ${complaint.status}",
                color = if (complaint.status == "Resolved") Color(0xFF2E7D32) else Color(0xFFD84315)
            )

            Spacer(Modifier.height(4.dp))
            Text(complaint.addressText)

            Spacer(Modifier.height(8.dp))

            // BEFORE PHOTO
            complaint.beforePhoto?.let {
                Image(
                    painter = rememberAsyncImagePainter(imageBaseUrl + it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // AFTER PHOTO (only if resolved)
            if (complaint.status == "Resolved" && complaint.afterPhoto != null) {
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(imageBaseUrl + complaint.afterPhoto),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // FEEDBACK BUTTON
            if (complaint.status == "Resolved") {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onGiveFeedback(complaint._id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Give Feedback")
                }
            }
        }
    }
}
