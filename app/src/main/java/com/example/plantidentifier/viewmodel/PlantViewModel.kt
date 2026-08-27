package com.example.plantidentifier.viewmodel

import android.util.Log
import com.example.plantidentifier.BuildConfig
import com.example.plantidentifier.R
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.Locale
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantidentifier.data.api.RetrofitClient
import com.example.plantidentifier.data.repository.PlantRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

data class PlantIdentificationUiState(
    val scientificName: String = "",
    val commonName: String = "",
    val family: String = "",
    val score: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val errorMessageRes: Int? = null
)

class PlantViewModel : ViewModel() {

    private val repository = PlantRepository(RetrofitClient.api)

    private val _uiState = MutableLiveData(
        PlantIdentificationUiState()
    )

    val uiState: LiveData<PlantIdentificationUiState> = _uiState

    fun identifyPlant(imageFile: File, organ: String = "leaf") {

        _uiState.value = PlantIdentificationUiState(
            isLoading = true
        )

        viewModelScope.launch {

            try {
                val requestFile = imageFile
                    .asRequestBody("image/jpeg".toMediaType())

                val imagePart = MultipartBody.Part.createFormData(
                    "images",
                    imageFile.name,
                    requestFile
                )

                val organsPart = organ
                    .takeUnless { it == "unknown" }
                    ?.toRequestBody("text/plain".toMediaType())

                Log.d(
                    "PlantIdentifier",
                    "Sending organ filter: ${organ.takeUnless { it == "unknown" } ?: "none (unknown)"}"
                )

                val appLanguage = Locale.getDefault().language
                Log.d("PlantIdentifier", "Sending app language: $appLanguage")

                val response = repository.identifyPlant(
                    image = imagePart,
                    organs = organsPart,
                    organForDebug = organ,
                    proxyToken = BuildConfig.PLANT_PROXY_TOKEN,
                    appLanguage = appLanguage
                )

                val result = response.results?.firstOrNull()

                if (result != null) {

                    val species = result.species

                    _uiState.value = PlantIdentificationUiState(
                        scientificName =
                            response.bestMatch
                                ?: species?.scientificName
                                ?: "Unknown species",

                        commonName =
                            species?.commonNames
                                ?.firstOrNull()
                                ?: "Unknown plant",

                        family =
                            species?.family
                                ?.scientificName
                                ?: "Unknown family",

                        score = result.score ?: 0.0,
                        isLoading = false
                    )

                } else {

                    _uiState.value = PlantIdentificationUiState(
                        error = "no_matches",
                        errorMessageRes = R.string.no_matches_error
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "PlantIdentifier",
                    "Plant identification failed",
                    e
                )

                val messageRes = when (e) {
                    is SocketTimeoutException, is ConnectException, is IOException ->
                        R.string.plant_network_error
                    else -> R.string.plant_generic_error
                }
                _uiState.value = PlantIdentificationUiState(
                    error = "identification_failed",
                    errorMessageRes = messageRes
                )
            }
        }
    }
}
