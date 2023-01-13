package pora.data.proj.networking

import okhttp3.RequestBody
import okhttp3.ResponseBody
import pora.data.proj.models.BusStationsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @GET("/busStations")
    fun busStations(): Call<List<BusStationsResponse>>

    @Multipart
    @POST("upload")
    fun upload(
        @Part("description") description: RequestBody?,
        @Part file: Part?
    ): Call<ResponseBody?>?

//    @GET("/shows/{id}")
//    fun getShow(@Path("id") id: String): Call<ShowResponse>
//
//    @GET("/shows/{show_id}/reviews")
//    fun getReviews(@Path("show_id") id: String): Call<ReviewsResponse>
//
//    @POST("/reviews/")
//    fun createReview(@Body review: ReviewReguest): Call<ReviewResponse>
}