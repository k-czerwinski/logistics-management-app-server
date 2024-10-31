package pl.edu.agh.routes

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.ApplicationTestBuilder
import io.mockk.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pl.edu.agh.KtorTestBase
import pl.edu.agh.dto.OrderDTO
import pl.edu.agh.dto.OrderListViewDTO
import pl.edu.agh.dto.ProductCreateDTO
import pl.edu.agh.model.*
import pl.edu.agh.dto.UserListViewItemDTO
import pl.edu.agh.plugins.configureSecurity
import pl.edu.agh.repositories.OrderRepository
import pl.edu.agh.repositories.ProductRepository
import pl.edu.agh.repositories.UserRepository
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AdminRoutesTest : KtorTestBase() {
    private val productRepository = mockk<ProductRepository>()
    private val userRepository = mockk<UserRepository>()
    private val orderRepository = mockk<OrderRepository>()

    private val companyId = 1
    private val adminId = 1

    val company = Company(companyId, "Company 1", "sample-company.com")
    val user = User(adminId, "Admin", "User", "admin.user", "password", UserRole.ADMIN, company)
    val products = listOf(
        Product(1, "Product 1", BigDecimal(12), "Description 1", company),
        Product(2, "Product 2", BigDecimal(13), "Description 2", company)
    )
    val orders = listOf(
        Order(
            id = 1,
            companyId = companyId,
            products = products.map { ProductEntry(it, 10) },
            client = user,
            name = "Order 1",
            placedOn = LocalDateTime(2024, 10, 1, 10, 0),
            sendOn = LocalDateTime(2024, 10, 2, 10, 0),
            deliveredOn = LocalDateTime(2024, 10, 3, 10, 0),
            expectedDeliveryOn = LocalDateTime(2024, 10, 3, 10, 0),
            courier = user,
            totalPrice = BigDecimal(40)
        )
    )
    val ordersJson = Json.encodeToString(orders.map(::OrderListViewDTO))

    private fun adminRoutesTestApplication(test: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
        customTestApplication {
            application {
                configureSecurity(jwtProperties)
                install(ContentNegotiation) {
                    json()
                }
            }
            routing {
                authenticate {
                    adminRoutes(productRepository, userRepository, orderRepository)
                }
            }
            mockkStatic(::getIntPathParam)
            every { getIntPathParam(any(), "companyId") } returns companyId
            every { getIntPathParam(any(), "adminId") } returns adminId
            test(client)
        }

    @Test
    fun `adminRoutes require authentication plugin`() = customTestApplication {
        // given
        routing {
            adminRoutes(productRepository, userRepository, orderRepository)
        }
        // then
        assertFailsWith<IllegalAccessException> {
            client.get("/admin/$adminId/orders")
        }
    }

    @Test
    fun `test get orders`() = adminRoutesTestApplication {
        // given
        val accessToken = jwtTokenBuilder.accessToken(adminId, UserRole.ADMIN, companyId)
        coEvery { orderRepository.getAll(companyId) } returns orders
        // when
        val response = client.get("/admin/$adminId/orders") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ordersJson, response.bodyAsText())
    }

    @Test
    fun `test get order by id`() = adminRoutesTestApplication {
        // given
        val order = orders[0]
        val accessToken = jwtTokenBuilder.accessToken(adminId, UserRole.ADMIN, companyId)
        coEvery { orderRepository.getById(order.id, companyId) } returns order
        every { getIntPathParam(any(), "orderId") } returns order.id
        // when
        val response = client.get("/admin/$adminId/order/${order.id}") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(Json.encodeToString(OrderDTO(order)), response.bodyAsText())
    }

    @Test
    fun `test add new product`() = adminRoutesTestApplication {
        // given
        val productCreateDTO = ProductCreateDTO("Product 3", BigDecimal(20), "Description 3")
        val accessToken = jwtTokenBuilder.accessToken(adminId, UserRole.ADMIN, companyId)
        coEvery { productRepository.add(productCreateDTO, companyId) } returns products[0]
        // when
        val response = client.post("/admin/$adminId/product") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(productCreateDTO))
        }
        // then
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `test get users`() = adminRoutesTestApplication {
        // given
        val users = listOf(user)
        val usersJson = Json.encodeToString(users.map(UserListViewItemDTO::toDTO))
        val accessToken = jwtTokenBuilder.accessToken(adminId, UserRole.ADMIN, companyId)
        coEvery { userRepository.getAll(companyId) } returns users
        // when
        val response = client.get("/admin/$adminId/users") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(usersJson, response.bodyAsText())
    }

    @Test
    fun `test get order by id with non-existent order`() = adminRoutesTestApplication {
        // given
        val orderId = 999
        val accessToken = jwtTokenBuilder.accessToken(adminId, UserRole.ADMIN, companyId)
        coEvery { orderRepository.getById(orderId, companyId) } returns null
        every { getIntPathParam(any(), "orderId") } returns orderId
        // when
        val response = client.get("/admin/$adminId/order/$orderId") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}