package pl.edu.agh.model

enum class UserRole {
    ADMIN, CLIENT, COURIER;

    companion object {
        fun valueOfNullable(value: String): UserRole? {
            return try {
                valueOf(value)
            } finally {
                null
            }
        }
    }
}