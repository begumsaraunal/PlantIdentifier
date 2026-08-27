package com.example.plantidentifier.data.repository

import com.example.plantidentifier.data.api.PlantNetApi
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PlantRepository(
    private val api: PlantNetApi
) {

    suspend fun identifyPlant(
        image: MultipartBody.Part,
        organs: RequestBody?,
        organForDebug: String,
        proxyToken: String,
        appLanguage: String
    ) = api.identifyPlant(
        image = image,
        organs = organs,
        organForDebug = organForDebug,
        proxyToken = proxyToken,
        appLanguage = appLanguage
    )
}
