package pl.edu.agh.repositories

import org.jetbrains.exposed.sql.and
import pl.edu.agh.dao.CompanyDAO
import pl.edu.agh.dao.UserDAO
import pl.edu.agh.dao.UserTable
import pl.edu.agh.model.User
import pl.edu.agh.dto.UserCreateDTO
import pl.edu.agh.model.UserRole

class UserRepository : Repository<User, UserCreateDTO> {
    suspend fun getByUsername(username: String, companyId: Int): User? = suspendTransaction {
        UserDAO.find { (UserTable.username eq username) and (UserTable.company eq companyId) }.firstOrNull()
            ?.let(::User)
    }

    override suspend fun getAll(companyId: Int): List<User> = suspendTransaction {
        UserDAO.find{ UserTable.company eq companyId}.map(::User)
    }

    override suspend fun getById(entityId: Int, companyId: Int): User? = suspendTransaction {
        UserDAO.find { (UserTable.id eq entityId) and (UserTable.company eq companyId) }.firstOrNull()?.let(::User)
    }

    suspend fun getByRole(companyId: Int, userRole: UserRole) : List<User> = suspendTransaction {
        UserDAO.find{ (UserTable.company eq companyId) and (UserTable.role eq userRole) }.map(::User)
    }

    override suspend fun add(item: UserCreateDTO, companyId: Int): User = suspendTransaction {
        val userDAO = UserDAO.new {
            username = item.username
            password = item.hashedPassword()
            firstName = item.firstName
            lastName = item.lastName
            companyDAO = CompanyDAO[companyId]
            role = item.role
        }
        User(userDAO)
    }
}