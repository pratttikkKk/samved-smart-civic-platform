package com.example.samved.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.samved.api.ApiClient
import com.example.samved.model.Complaint
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllComplaintsScreen(userId: String) {

    val scope = rememberCoroutineScope()
    var complaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val res = ApiClient.api.getAllComplaints()
        if (res.isSuccessful) complaints = res.body() ?: emptyList()
        loading = false
    }

    val filteredComplaints = complaints
        .filter {
            when (selectedTab) {
                0 -> it.status.equals("Pending", true)
                1 -> it.status.equals("In Progress", true)
                else -> it.status.equals("Resolved", true)
            }
        }
        .sortedByDescending { it.priorityScore }

    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F2027), Color(0xFF203A43))
                )
            )
    ) {

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selectedTab == 0, onClick = { selectedTab = 0 }) {
                Icon(Icons.Default.Warning, null)
                Text("Pending")
            }
            Tab(selectedTab == 1, onClick = { selectedTab = 1 }) {
                Icon(Icons.Default.HourglassTop, null)
                Text("In Progress")
            }
            Tab(selectedTab == 2, onClick = { selectedTab = 2 }) {
                Icon(Icons.Default.CheckCircle, null)
                Text("Resolved")
            }
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredComplaints.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No complaints found", color = Color.White)
            }
        } else {
            LazyColumn(Modifier.padding(12.dp)) {
                items(filteredComplaints) { complaint ->
                    AllComplaintCard(
                        complaint = complaint,
                        userId = userId,
                        onConfirm = {
                            scope.launch {
                                ApiClient.api.confirmComplaint(
                                    complaint._id,
                                    mapOf("userId" to userId)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AllComplaintCard(
    complaint: Complaint,
    userId: String,
    onConfirm: () -> Unit
) {
    val imageBaseUrl = "http://10.0.2.2:5000/uploads/"
    val alreadyReported = complaint.reporters.contains(userId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    complaint.issueType,
                    style = MaterialTheme.typography.titleLarge
                )
                AssistChip(
                    onClick = {},
                    label = { Text("Priority ${complaint.priorityScore}") }
                )
            }

            Text(
                complaint.addressText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(8.dp))

            Image(
                painter = rememberAsyncImagePainter(imageBaseUrl + complaint.beforePhoto),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )

            if (complaint.status.equals("Pending", true) && !alreadyReported) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text("Confirm Issue")
                }
            }

            if (alreadyReported) {
                Text(
                    "Already confirmed by you",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
