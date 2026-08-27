package com.example.plantidentifier.data.api

import com.example.plantidentifier.data.model.PlantResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Header

interface PlantNetApi {

    @Multipart
    @POST("v2/identify/all")
    suspend fun identifyPlant(
        @Part image: MultipartBody.Part,
        @Part("organs") organs: RequestBody?,
        @Header("X-Plant-Organ") organForDebug: String,
        @Header("X-Plant-Proxy-Token") proxyToken: String,
        @Header("X-App-Language") appLanguage: String
    ): PlantResponse
}
