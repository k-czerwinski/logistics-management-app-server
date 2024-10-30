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
import pl.edu.agh.model.*
import pl.edu.agh.plugins.configureSecurity
import pl.edu.agh.repositories.OrderRepository
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClientRoutesTest : KtorTestBase() {
    private val orderRepository = mockk<OrderRepository>()

    private val companyId = 1
    private val clientId = 1

    val company = Company(companyId, "Company 1", "sample-company.com")
    val user = User(clientId, "FirstName", "LastName", "username", "password", UserRole.CLIENT, 1)
    val courier = User(2, "FirstName", "LastName", "username", "password", UserRole.COURIER, 1)
    val products = listOf(
        Product(1, "Product 1", BigDecimal(12), "Description 1", company),
        Product(2, "Product 2", BigDecimal(13), "Description 2", company),
        Product(3, "Product 3", BigDecimal(30), "Description 3", company),
        Product(4, "Product 4", BigDecimal(50), "Description 4", company)
    )
    val orders = listOf(
        Order(
            id = 1,
            companyId = companyId,
            products = products.shuffled().take(2).map { ProductEntry(ProductDTO(it), 10) },
            client = user,
            name = "Order 1",
            placedOn = LocalDateTime(2024, 10, 1, 10, 0),
            sendOn = LocalDateTime(2024, 10, 2, 10, 0),
            deliveredOn = LocalDateTime(2024, 10, 3, 10, 0),
            expectedDeliveryOn = LocalDateTime(2024, 10, 3, 10, 0),
            courier = courier,
            totalPrice = BigDecimal(40)
        ),
        Order(
            id = 1,
            companyId = companyId,
            products = products.shuffled().take(1).map { ProductEntry(ProductDTO(it), 10) },
            client = user,
            name = "Order 1",
            placedOn = LocalDateTime(2024, 10, 1, 10, 0),
            sendOn = LocalDateTime(2024, 10, 2, 10, 0),
            deliveredOn = LocalDateTime(2024, 10, 3, 10, 0),
            expectedDeliveryOn = LocalDateTime(2024, 10, 3, 10, 0),
            courier = courier,
            totalPrice = BigDecimal(40)
        ),
        Order(
            id = 1,
            companyId = companyId,
            products = products.map { ProductEntry(ProductDTO(it), 10) },
            client = user,
            name = "Order 1",
            placedOn = LocalDateTime(2024, 10, 1, 10, 0),
            sendOn = LocalDateTime(2024, 10, 2, 10, 0),
            deliveredOn = LocalDateTime(2024, 10, 3, 10, 0),
            expectedDeliveryOn = LocalDateTime(2024, 10, 3, 10, 0),
            courier = courier,
            totalPrice = BigDecimal(40)
        ),
    )
    val ordersJson = Json.encodeToString(orders.map { it.toOrderListView() })

    private fun clientRoutesTestApplication(test: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
        customTestApplication {
            application {
                configureSecurity(jwtProperties)
                install(ContentNegotiation) {
                    json()
                }
            }
            routing {
                authenticate {
                    clientRoutes(orderRepository)
                }
            }
            mockkStatic(::getIntPathParam)
            every { getIntPathParam(any(), "companyId") } returns companyId
            every { getIntPathParam(any(), "clientId") } returns clientId
            test(client)
        }

    @Test
    fun `clientRoutes require authentication plugin`() = customTestApplication {
        // given
        routing {
            clientRoutes(orderRepository)
        }
        // then
        assertFailsWith<IllegalAccessException> {
            client.get("/client/$clientId/orders")
        }
    }

    @Test
    fun `test get orders`() = clientRoutesTestApplication {
        // given
        val accessToken = jwtTokenBuilder.accessToken(clientId, UserRole.CLIENT, companyId)
        coEvery { orderRepository.getAllByClientId(clientId, companyId) } returns orders
        // when
        val response = client.get("/client/$clientId/orders") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ordersJson, response.bodyAsText())
    }

    @Test
    fun `test get order by id`() = clientRoutesTestApplication {
        // given
        val order = orders[0]
        val accessToken = jwtTokenBuilder.accessToken(clientId, UserRole.CLIENT, companyId)
        coEvery { orderRepository.getById(order.id, companyId) } returns order
        every { getIntPathParam(any(), "orderId") } returns order.id
        // when
        val response = client.get("/client/$clientId/order/${order.id}") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(Json.encodeToString(order), response.bodyAsText())
    }

    @Test
    fun `test add new order`() = clientRoutesTestApplication {
        // given
        val orderCreateDTO = OrderCreateDTO(listOf(), clientId, "Order 1")
        val orderCreateResponseDTO = OrderCreateResponseDTO(BigDecimal(40))
        val accessToken = jwtTokenBuilder.accessToken(clientId, UserRole.CLIENT, companyId)
        coEvery { orderRepository.add(orderCreateDTO, companyId) } returns orders[0]
        // when
        val response = client.post("/client/$clientId/order") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(orderCreateDTO))
        }
        // then
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(Json.encodeToString(orderCreateResponseDTO), response.bodyAsText())
    }

    @Test
    fun `test get order by id with non-existent order`() = clientRoutesTestApplication {
        // given
        val orderId = 999
        val accessToken = jwtTokenBuilder.accessToken(clientId, UserRole.CLIENT, companyId)
        coEvery { orderRepository.getById(orderId, companyId) } returns null
        every { getIntPathParam(any(), "orderId") } returns orderId
        // when
        val response = client.get("/client/$clientId/order/$orderId") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}