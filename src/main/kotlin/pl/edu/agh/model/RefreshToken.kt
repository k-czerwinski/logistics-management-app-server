package pl.edu.agh.model

import kotlinx.datetime.LocalDateTime

data class RefreshToken(val user: User, val token: String, val expiryDate: LocalDateTime)