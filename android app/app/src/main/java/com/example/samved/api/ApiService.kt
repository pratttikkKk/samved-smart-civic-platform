package com.example.samved.api
import com.example.samved.api.FeedbackRequest
import com.example.samved.model.Complaint
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class LoginRequest(val mobile: String, val password: String)
data class RegisterRequest(val name: String, val mobile: String, val ward: String, val password: String)

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<Any>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<Any>

    @GET("complaints/user/{userId}")
    suspend fun getMyComplaints(@Path("userId") userId: String): Response<List<Complaint>>



    @POST("feedback")
    suspend fun sendFeedback(@Body request: FeedbackRequest): Response<Any>


    @GET("complaints")
    suspend fun getAllComplaints(): Response<List<Complaint>>

    @POST("complaints/{id}/confirm")
    suspend fun confirmComplaint(
        @Path("id") complaintId: String,
        @Body body: Map<String, String>
    ): Response<Any>



    @Multipart
    @POST("complaints")
    suspend fun uploadComplaint(
        @Part("userId") userId: RequestBody,
        @Part("issueType") issueType: RequestBody,
        @Part("description") description: RequestBody,
        @Part("ward") ward: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("addressText") address: RequestBody,
        @Part photo: MultipartBody.Part
    ): Response<Any>
}

    object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:5000/api/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
            .create(ApiService::class.java)
    }
}
