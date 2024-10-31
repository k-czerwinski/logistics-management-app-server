package pl.edu.agh.dto

import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt
import pl.edu.agh.model.User
import pl.edu.agh.model.UserRole

@Serializable
data class UserCreateDTO(
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String,
    val role: UserRole
) {
    fun hashedPassword() : String = BCrypt.hashpw(password, BCrypt.gensalt())
}

@Serializable
data class UserListViewItemDTO(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
) {
    companion object {
        fun toDTO(user: User) : UserListViewItemDTO {
            return UserListViewItemDTO(
                id = user.id,
                firstName = user.firstName,
                lastName = user.lastName,
                role = user.role
            )
        }
    }
}

@Serializable
data class UserDTO(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val username: String,
    val role: UserRole,
    val companyId: Int
) {
    constructor(user: User) : this(
        id = user.id,
        firstName = user.firstName,
        lastName = user.lastName,
        username = user.username,
        role = user.role,
        companyId = user.company.id
    )
}