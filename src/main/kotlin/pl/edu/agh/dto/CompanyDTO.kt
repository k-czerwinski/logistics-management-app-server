package pl.edu.agh.dto

import kotlinx.serialization.Serializable

@Serializable
data class CompanyDTO(
    val name: String,
    val domain: String
)