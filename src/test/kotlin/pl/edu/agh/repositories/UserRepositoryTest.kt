package pl.edu.agh.repositories

import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.BeforeClass
import org.junit.Test
import pl.edu.agh.dao.CompanyDAO
import pl.edu.agh.dao.CompanyTable
import pl.edu.agh.dao.UserDAO
import pl.edu.agh.dao.UserTable
import pl.edu.agh.dto.UserCreateDTO
import pl.edu.agh.model.Company
import pl.edu.agh.model.User
import pl.edu.agh.model.UserRole
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryTest {
    companion object {
        lateinit var userRepository: UserRepository

        @BeforeClass
        @JvmStatic
        fun setup() {
            val applicationConfig = ApplicationConfig("db-test.conf")
            Database.connect(
                url = applicationConfig.property("ktor.database.url").getString(),
                driver = applicationConfig.property("ktor.database.driver").getString(),
                user = applicationConfig.property("ktor.database.user").getString(),
                password = applicationConfig.property("ktor.database.password").getString()
            )
            transaction {
                SchemaUtils.create(
                    CompanyTable,
                    UserTable
                )
            }
            userRepository = UserRepository()
        }
    }

    val companies = listOf(
        Company(1, "Company 1", "sample-company.com"),
        Company(2, "Company 2", "another-company.com")
    )

    val users = listOf(
        User(1, "First1", "Last1", "user1", "Password123##", UserRole.ADMIN, companies[0]),
        User(2, "First2", "Last2", "user2", "Password123##", UserRole.CLIENT, companies[1])
    )

    @BeforeTest
    fun setUp() {
        transaction {
            companies.forEach {
                CompanyDAO.new(it.id) {
                    name = it.name
                    domain = it.domain
                }
            }
            users.forEach {
                UserDAO.new(it.id) {
                    username = it.username
                    password = it.password
                    firstName = it.firstName
                    lastName = it.lastName
                    companyDAO = CompanyDAO[it.company.id]
                    role = it.role
                }
            }
        }
    }

    @AfterTest
    fun tearDown() {
        transaction {
            UserDAO.all().forEach { it.delete() }
            CompanyDAO.all().forEach { it.delete() }
        }
    }

    @Test
    fun `test getAll returns all users for a company`() = runTest {
        // given
        val companyId = companies[0].id

        // when
        val result = userRepository.getAll(companyId)

        // then
        assertEquals(listOf(users[0]), result)
    }

    @Test
    fun `test getById returns user when found`() = runTest {
        // given
        val user = users[0]

        // when
        val result = userRepository.getById(user.id, user.company.id)

        // then
        assertEquals(user, result)
    }

    @Test
    fun `test getById returns null when user not found`() = runTest {
        // given
        val userId = -1
        val companyId = companies[0].id

        // when
        val result = userRepository.getById(userId, companyId)

        // then
        assertNull(result)
    }

    @Test
    fun `test add creates new user`() = runTest {
        // given
        val userDTO = UserCreateDTO("user3", "password3", "First3", "Last3", UserRole.COURIER)
        val companyId = companies[0].id

        // when
        userRepository.add(userDTO, companyId)

        // then
        val user = transaction {
            UserDAO.find { UserTable.username eq userDTO.username }.firstOrNull()
        }
        assertNotNull(user)
        assertEquals(userDTO.username, user.username)
        assertEquals(userDTO.firstName, user.firstName)
        assertEquals(userDTO.lastName, user.lastName)
        assertEquals(userDTO.role, user.role)
    }
}