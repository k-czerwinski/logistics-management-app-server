package pl.edu.agh.model

import pl.edu.agh.dao.UserDAO

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String,
    val role: UserRole,
    val company: Company
) {
    constructor(userDAO: UserDAO) : this(
        id = userDAO.id.value,
        firstName = userDAO.firstName,
        lastName = userDAO.lastName,
        username = userDAO.username,
        password = userDAO.password,
        role = userDAO.role,
        company = Company(userDAO.companyDAO)
    )
}
