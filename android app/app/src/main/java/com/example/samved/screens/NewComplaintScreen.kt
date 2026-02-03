package com.example.samved.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.samved.api.ApiClient
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewComplaintScreen(userId: String) {

    /* ---------------- CONTEXT & SERVICES ---------------- */
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    /* ---------------- STATE ---------------- */
    var issueType by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var ward by remember { mutableStateOf("") }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var address by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    /* ---------------- IMAGE PICKER ---------------- */
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri = it }

    /* ---------------- LOCATION ---------------- */
    val locationRequest = remember {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000
        ).setMaxUpdates(1).build()
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                latitude = loc.latitude
                longitude = loc.longitude

                val geocoder = Geocoder(context, Locale.getDefault())
                val addr = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                if (!addr.isNullOrEmpty()) {
                    address = addr[0].getAddressLine(0)
                }
            }
        }
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fusedClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }

    /* ---------------- UI THEME ---------------- */
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(20.dp)
    ) {

        Text(
            "Report Civic Issue",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(20.dp))

        /* ---------------- ISSUE TYPE ---------------- */
        ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
            OutlinedTextField(
                value = issueType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Issue Type") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded, { expanded = false }) {
                listOf(
                    "Garbage",
                    "Road Damage",
                    "Water Leakage",
                    "Sewage Overflow",
                    "Street Light",
                    "Other"
                ).forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            issueType = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Describe the issue") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = ward,
            onValueChange = { ward = it },
            label = { Text("Ward Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        /* ---------------- ACTION BUTTONS ---------------- */
        Button(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp)
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (imageUri == null) "Attach Photo" else "Photo Selected")
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    fusedClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Get Live Location")
        }

        AnimatedVisibility(address.isNotEmpty()) {
            Text("📍 $address", color = Color.White)
        }

        Spacer(Modifier.height(20.dp))

        /* ---------------- SUBMIT ---------------- */
        Button(
            onClick = {
                scope.launch {

                    if (
                        issueType.isBlank() ||
                        description.isBlank() ||
                        ward.isBlank() ||
                        imageUri == null ||
                        latitude == null ||
                        longitude == null
                    ) {
                        message = "Please fill all details"
                        return@launch
                    }

                    loading = true

                    val file = FileUtil.from(context, imageUri!!)
                    val imagePart = MultipartBody.Part.createFormData(
                        "photo",
                        file.name,
                        file.readBytes().toRequestBody("image/*".toMediaType())
                    )

                    val res = ApiClient.api.uploadComplaint(
                        userId.toRequestBody(), // ✅ FIXED
                        issueType.toRequestBody(),
                        description.toRequestBody(),
                        ward.toRequestBody(),
                        latitude.toString().toRequestBody(),
                        longitude.toString().toRequestBody(),
                        address.toRequestBody(),
                        imagePart
                    )

                    loading = false
                    message =
                        if (res.isSuccessful)
                            "Complaint submitted successfully"
                        else
                            "Submission failed (${res.code()})"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            enabled = !loading
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (loading) "Submitting..." else "Submit Complaint")
        }

        Spacer(Modifier.height(8.dp))
        Text(message, color = Color.White)
    }
}

/* ---------------- FILE UTIL ---------------- */
object FileUtil {
    fun from(context: Context, uri: Uri): File {
        val input = context.contentResolver.openInputStream(uri)!!
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { input.copyTo(it) }
        return file
    }
}
