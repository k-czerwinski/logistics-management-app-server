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
import pl.edu.agh.dto.CompanyDTO
import pl.edu.agh.model.Company
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CompanyRepositoryTest {
    companion object {
        lateinit var companyRepository: CompanyRepository

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
                    CompanyTable
                )
            }
            companyRepository = CompanyRepository()
        }
    }

    val companies = listOf(
        Company(1, "Company 1", "sample-company.com"),
        Company(2, "Company 2", "another-company.com")
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
        }
    }

    @AfterTest
    fun tearDown() {
        transaction {
            CompanyDAO.all().forEach { it.delete() }
        }
    }

    @Test
    fun `test getById returns company when found`() {
        runTest {
            // given
            val company = companies[0]

            // when
            val result = companyRepository.getById(company.id, company.id)

            // then
            assertEquals(company, result)
        }
    }

    @Test
    fun `test getById returns null when company not found`() = runTest {
        // given
        val companyId = -1

        // when
        val result = companyRepository.getById(companyId, companyId)

        // then
        assertNull(result)
    }

    @Test
    fun `test getById throws IllegalArgumentException when id do not match`() = runTest {
        assertFailsWith<IllegalArgumentException>("Company id must be equal to entity id") {
            companyRepository.getById(1, 2)
        }
    }

    @Test
    fun `test getByDomain returns company when found`() = runTest {
        // given
        val company = companies[0]
        val domain = company.domain
        // when
        val result = companyRepository.getByDomain(domain)

        // then
        assertEquals(company, result)
    }

    @Test
    fun `test getByDomain returns null when company not found`() = runTest {
        // given
        val domain = "non-existing-domain.com"
        // when
        val result = companyRepository.getByDomain(domain)
        // then
        assertNull(result)
    }

    @Test
    fun `test add creates new company`() = runTest {
        // given
        val companyDTO = CompanyDTO("Company Name", "sample-company-domain.com")
        // when
        companyRepository.add(companyDTO)
        // then
        val company = transaction {
            CompanyDAO.find { CompanyTable.domain eq companyDTO.domain }.firstOrNull()
        }
        assertNotNull(company)
        assertEquals(companyDTO.name, company.name)
        assertEquals(companyDTO.domain, company.domain)
    }
}