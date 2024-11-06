package pl.edu.agh.dto

import kotlinx.serialization.Serializable

@Serializable
data class CompanyCreateDTO(
    val name: String,
    val domain: String
)