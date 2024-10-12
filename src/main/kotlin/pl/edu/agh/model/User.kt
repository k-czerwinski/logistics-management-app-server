package pl.edu.agh.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.mindrot.jbcrypt.BCrypt
import pl.edu.agh.dao.UserDAO

@Serializable
data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val username: String,
    @Transient
    val password: String = "",
    val role: UserRole,
    val companyId: Int
)

@Serializable
data class UserCreateDTO(
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String,
    val role: UserRole,
    val companyId: Int
) {
    fun hashedPassword() : String = BCrypt.hashpw(password, BCrypt.gensalt())
}

fun toUser(dao: UserDAO) = User(
    id = dao.id.value,
    firstName = dao.firstName,
    lastName = dao.lastName,
    username = dao.username,
    password = dao.password,
    role = dao.role,
    companyId = dao.companyDAO.id.value
)
