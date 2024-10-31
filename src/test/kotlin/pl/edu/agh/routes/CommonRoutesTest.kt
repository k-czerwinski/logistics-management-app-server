package pl.edu.agh.routes

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.ApplicationTestBuilder
import io.mockk.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pl.edu.agh.KtorTestBase
import pl.edu.agh.dto.UserDTO
import pl.edu.agh.model.Company
import pl.edu.agh.model.Product
import pl.edu.agh.dto.ProductDTO
import pl.edu.agh.model.User
import pl.edu.agh.model.UserRole
import pl.edu.agh.plugins.configureSecurity
import pl.edu.agh.repositories.ProductRepository
import pl.edu.agh.repositories.UserRepository
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommonRoutesTest : KtorTestBase() {
    private val productRepository = mockk<ProductRepository>()
    private val userRepository = mockk<UserRepository>()

    val company = Company(1, "Company 1", "sample-company.com")
    val products = listOf(
        Product(1, "Product 1", BigDecimal(12), "Description 1", company),
        Product(2, "Product 2", BigDecimal(13), "Description 2", company)
    )
    val productsJson = Json.encodeToString(products.map { ProductDTO(it) })

    private val userId = 1
    private val companyId = 1

    private fun commonRoutesTestApplication(test: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
        customTestApplication {
            application {
                configureSecurity(jwtProperties)
                install(ContentNegotiation) {
                    json()
                }
            }
            routing {
                authenticate {
                    commonRoutes(productRepository, userRepository)
                }
            }
            mockkStatic(::getIntPathParam)
            every { getIntPathParam(any(), "companyId") } returns companyId
            test(client)
        }

    @Test
    fun `commonRoutes require authentication plugin`() = customTestApplication {
        // given
        routing {
            commonRoutes(productRepository, userRepository)
        }
        // then
        assertFailsWith<IllegalAccessException> {
            client.get("/client/products")
        }
    }

    @Test
    fun `test product route`() = commonRoutesTestApplication {
        // given
        val accessToken = jwtTokenBuilder.accessToken(userId, UserRole.CLIENT, companyId)
        coEvery { productRepository.getAll(companyId) } returns products
        // when
        val response = client.get("/client/products") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(productsJson, response.bodyAsText())
    }

    @Test
    fun `test get product by id`() = commonRoutesTestApplication {
        // given
        val product = products.get(0)
        val accessToken = jwtTokenBuilder.accessToken(1, UserRole.CLIENT, 1)
        coEvery { productRepository.getById(product.id, companyId) } returns product
        every { getIntPathParam(any(), "productId") } returns product.id

        // when
        val response = client.get("/client/product/${product.id}") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(Json.encodeToString(ProductDTO(product)), response.bodyAsText())
    }

    @Test
    fun `test get user`() = commonRoutesTestApplication {
        // given
        val user = User(userId, "FirstName", "LastName", "username", "password", UserRole.CLIENT, company)
        val accessToken = jwtTokenBuilder.accessToken(1, UserRole.CLIENT, 1)
        coEvery { userRepository.getById(userId, companyId) } returns user
        // when
        val response = client.get("/client") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(Json.encodeToString(UserDTO(user)), response.bodyAsText())
    }

    @Test
    fun `test commonRoutes are exposed for all roles`() {
        performTest(UserRole.CLIENT)
        performTest(UserRole.ADMIN)
        performTest(UserRole.COURIER)
    }

    private fun performTest(userRole: UserRole) = commonRoutesTestApplication {
        // given
        val user = User(userId, "FirstName", "LastName", "username", "password", userRole, company)
        val accessToken = jwtTokenBuilder.accessToken(1, userRole, 1)
        coEvery { userRepository.getById(userId, companyId) } returns user
        // when
        val response = client.get("/" + userRole.name.lowercase()) {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(Json.encodeToString(UserDTO(user)), response.bodyAsText())
    }
}