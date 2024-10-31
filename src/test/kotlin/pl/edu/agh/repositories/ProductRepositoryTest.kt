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
import pl.edu.agh.dao.ProductDAO
import pl.edu.agh.dao.ProductTable
import pl.edu.agh.dto.ProductCreateDTO
import pl.edu.agh.model.Company
import pl.edu.agh.model.Product
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProductRepositoryTest {
    companion object {
        lateinit var productRepository: ProductRepository

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
                    ProductTable
                )
            }
            productRepository = ProductRepository()
        }
    }

    val companies = listOf(
        Company(1, "Company 1", "sample-company.com"),
        Company(2, "Company 2", "another-company.com")
    )

    val products = listOf(
        Product(1, "Product 1", BigDecimal(10.23), "Description 1", companies[0]),
        Product(2, "Product 2", BigDecimal(20.56), "Description 2", companies[1])
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
            products.forEach {
                ProductDAO.new(it.id) {
                    name = it.name
                    description = it.description
                    price = it.price
                    companyDAO = CompanyDAO[it.company.id]
                }
            }
        }
    }

    @AfterTest
    fun tearDown() {
        transaction {
            ProductDAO.all().forEach { it.delete() }
            CompanyDAO.all().forEach { it.delete() }
        }
    }

    @Test
    fun `test getAll returns all products for a company`() = runTest {
        // given
        val companyId = companies[0].id

        // when
        val result = productRepository.getAll(companyId)

        // then
        assertEquals(listOf(products[0]), result)
    }

    @Test
    fun `test getById returns product when found`() = runTest {
        // given
        val product: Product = products[0]

        // when
        val result = productRepository.getById(product.id, product.company.id)

        // then
        assertEquals(product, result)
    }

    @Test
    fun `test getById returns null when product not found`() = runTest {
        // given
        val productId = -1
        val companyId = companies[0].id

        // when
        val result = productRepository.getById(productId, companyId)

        // then
        assertNull(result)
    }

    @Test
    fun `test add creates new product`() = runTest {
        // given
        val productDTO = ProductCreateDTO("Product 3", BigDecimal(10.55), "Description 3")
        val companyId = companies[0].id

        // when
        productRepository.add(productDTO, companyId)

        // then
        val product = transaction {
           ProductDAO.find { ProductTable.name eq productDTO.name }.firstOrNull()
        }
        assertNotNull(product)
        assertEquals(productDTO.name, product.name)
        assertEquals(productDTO.description, product.description)
        assertEquals(productDTO.price.setScale(2, RoundingMode.DOWN), product.price)
    }
}
