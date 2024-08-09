package pl.edu.agh.model

import kotlinx.serialization.Serializable
import pl.edu.agh.dao.UserDAO

@Serializable
data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String,
    val role: UserRole,
    val temporaryPassword: Boolean,
    val company: Company
)

@Serializable
data class UserCreateDTO(
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String,
    val role: UserRole,
    val companyId: Int
)

fun toUser(dao: UserDAO) = User(
    id = dao.id.value,
    firstName = dao.firstName,
    lastName = dao.lastName,
    username = dao.username,
    password = dao.password,
    role = dao.role,
    temporaryPassword = dao.temporaryPassword,
    company = toCompany(dao.companyDAO)
)
