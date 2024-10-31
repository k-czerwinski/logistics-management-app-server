package pl.edu.agh.dto

import kotlinx.serialization.Serializable
import pl.edu.agh.model.UserRole

@Serializable
data class LoginRequest(val username: String, val password: String, val companyDomain: String)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val companyId: Int,
    val userId: Int,
    val userRole: UserRole
)

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class LogoutRequest(val refreshToken: String)

@Serializable
data class RefreshTokenResponse(val accessToken: String, val refreshToken: String)