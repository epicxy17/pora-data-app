package pora.data.proj.networking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import pora.data.proj.models.BusStationsResponse
import pora.data.proj.models.SpeedometerRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @GET("/busStations")
    fun busStations(): Call<List<BusStationsResponse>>

    @Multipart
    @POST("/images/upload")
    fun uploadImage(
        @Part("name") name: RequestBody,
        @Part("station_id") id: RequestBody,
        @Part("latitude") lat: RequestBody,
        @Part("longitude") long: RequestBody,
        @Part("date_time") dateTime: RequestBody,
        @Part image: MultipartBody.Part,
    ): Call<ResponseBody>

    @Multipart
    @POST("/voiceRecordings/upload")
    fun uploadVoiceRecording(
        @Part("name") name: RequestBody,
        @Part("latitude") lat: RequestBody,
        @Part("longitude") long: RequestBody,
        @Part("date_time") dateTime: RequestBody,
        @Part sound: MultipartBody.Part,
    ): Call<ResponseBody>

    @POST("/speedometers/upload")
    fun uploadSpeedometerData(@Body data: SpeedometerRequest): Call<ResponseBody>
}