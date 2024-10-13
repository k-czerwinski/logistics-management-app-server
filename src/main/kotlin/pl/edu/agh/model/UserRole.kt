package pl.edu.agh.model

import io.ktor.util.*

enum class UserRole {
    ADMIN, CLIENT, COURIER;

    companion object {
        fun valueOfNullable(value: String): UserRole? {
            return try {
                valueOf(value.toUpperCasePreservingASCIIRules())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}