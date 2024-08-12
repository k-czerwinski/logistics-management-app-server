package pl.edu.agh.repositories

import org.jetbrains.exposed.sql.and
import pl.edu.agh.dao.CompanyDAO
import pl.edu.agh.dao.UserDAO
import pl.edu.agh.dao.UserTable
import pl.edu.agh.model.User
import pl.edu.agh.model.UserCreateDTO
import pl.edu.agh.model.toUser

class UserRepository() : Repository<User, UserCreateDTO> {
    suspend fun getByUsername(username: String, companyId: Int): User? = suspendTransaction {
        UserDAO.find { (UserTable.username eq username) and (UserTable.company eq companyId) }.firstOrNull()?.let(::toUser)
    }
    override suspend fun getAll(): List<User> = suspendTransaction {
        UserDAO.all().map(::toUser)
    }

    override suspend fun getById(id: Int): User? = suspendTransaction {
        UserDAO.findById(id)?.let(::toUser)
    }

    override suspend fun add(item: UserCreateDTO): Unit = suspendTransaction {
        UserDAO.new {
            username = item.username
            password = item.password
            firstName = item.firstName
            lastName = item.lastName
            companyDAO = CompanyDAO[item.companyId]
            role = item.role
            temporaryPassword = false
         }
    }

    override suspend fun update(item: User): User {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }
}