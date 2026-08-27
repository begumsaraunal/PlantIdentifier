package com.example.plantidentifier.data.model

data class PlantResponse(
    val bestMatch: String?,
    val results: List<PlantResult>?
)

data class PlantResult(
    val score: Double?,
    val species: Species?
)

data class Species(
    val scientificName: String?,
    val scientificNameWithoutAuthor: String?,
    val commonNames: List<String>?,
    val genus: Genus?,
    val family: Family?
)

data class Genus(
    val scientificName: String?
)

data class Family(
    val scientificName: String?
)
