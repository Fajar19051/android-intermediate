package com.intermediate.submission1.data.network

import com.intermediate.submission1.data.network.response.AddStoryResponse
import com.intermediate.submission1.data.network.response.ListStoryResponse
import com.intermediate.submission1.data.network.response.LoginResponse
import com.intermediate.submission1.data.network.response.SignupResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun signup(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): SignupResponse


    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("stories?location=1")
    suspend fun getStoriesWithLocation(
        @Header("Authorization") token: String
    ): ListStoryResponse

    @GET("stories")
    suspend fun getPagedStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ListStoryResponse

    @Multipart
    @POST("stories")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") lon: RequestBody? = null
    ): AddStoryResponse
}