package pl.edu.agh.routes

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
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
import pl.edu.agh.dto.OrderExpectedDeliveryDTO
import pl.edu.agh.dto.OrderListViewDTO
import pl.edu.agh.model.*
import pl.edu.agh.plugins.configureSecurity
import pl.edu.agh.repositories.OrderRepository
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CourierRoutesTest : KtorTestBase() {
    private val orderRepository = mockk<OrderRepository>()

    private val companyId = 1
    private val courierId = 2

    val company = Company(companyId, "Company 1", "sample-company.com")
    val user = User(1, "FirstName", "LastName", "username", "password", UserRole.CLIENT, company)
    val courier = User(courierId, "FirstName", "LastName", "username", "password", UserRole.COURIER, company)
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
            products = products.shuffled().take(2).map { ProductEntry(it, 10) },
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
            id = 2,
            companyId = companyId,
            products = products.shuffled().take(1).map { ProductEntry(it, 10) },
            client = user,
            name = "Order 2",
            placedOn = LocalDateTime(2024, 10, 4, 10, 0),
            sendOn = LocalDateTime(2024, 10, 5, 10, 0),
            deliveredOn = LocalDateTime(2024, 10, 6, 10, 0),
            expectedDeliveryOn = LocalDateTime(2024, 10, 6, 10, 0),
            courier = courier,
            totalPrice = BigDecimal(30)
        ),
        Order(
            id = 3,
            companyId = companyId,
            products = products.map { ProductEntry(it, 10) },
            client = user,
            name = "Order 3",
            placedOn = LocalDateTime(2024, 10, 7, 10, 0),
            sendOn = LocalDateTime(2024, 10, 8, 10, 0),
            deliveredOn = LocalDateTime(2024, 10, 9, 10, 0),
            expectedDeliveryOn = LocalDateTime(2024, 10, 9, 10, 0),
            courier = courier,
            totalPrice = BigDecimal(170)
        ),
    )
    val ordersJson = Json.encodeToString(orders.map(::OrderListViewDTO))

    private fun courierRoutesTestApplication(test: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
        customTestApplication {
            application {
                configureSecurity(jwtProperties)
                install(ContentNegotiation) {
                    json()
                }
            }
            routing {
                authenticate {
                    courierRoutes(orderRepository)
                }
            }
            mockkStatic(::getIntPathParam)
            every { getIntPathParam(any(), "companyId") } returns companyId
            every { getIntPathParam(any(), "courierId") } returns courierId
            test(client)
        }

    @Test
    fun `courierRoutes require authentication plugin`() = customTestApplication {
        // given
        routing {
            courierRoutes(orderRepository)
        }
        // then
        assertFailsWith<IllegalAccessException> {
            client.get("/courier/$courierId/orders")
        }
    }

    @Test
    fun `test get orders`() = courierRoutesTestApplication {
        // given
        val accessToken = jwtTokenBuilder.accessToken(courierId, UserRole.COURIER, companyId)
        coEvery { orderRepository.getAllByCourierId(courierId, companyId) } returns orders
        // when
        val response = client.get("/courier/$courierId/orders") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ordersJson, response.bodyAsText())
    }

    @Test
    fun `test get order by id`() = courierRoutesTestApplication {
        // given
        val order = orders[0]
        val accessToken = jwtTokenBuilder.accessToken(courierId, UserRole.COURIER, companyId)
        coEvery { orderRepository.getById(order.id, companyId) } returns order
        every { getIntPathParam(any(), "orderId") } returns order.id
        // when
        val response = client.get("/courier/$courierId/order/${order.id}") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(Json.encodeToString(OrderDTO(order)), response.bodyAsText())
    }

    @Test
    fun `test set expected delivery`() = courierRoutesTestApplication {
        // given
        val order = orders[0]
        val expectedDelivery = LocalDateTime(2024, 10, 10, 10, 0)
        val accessToken = jwtTokenBuilder.accessToken(courierId, UserRole.COURIER, companyId)
        coEvery { orderRepository.setExpectedDelivery(companyId, order.id, courierId, expectedDelivery) } just Runs
        every { getIntPathParam(any(), "orderId") } returns order.id
        // when
        val response = client.put("/courier/$courierId/order/${order.id}/expected-delivery") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(OrderExpectedDeliveryDTO(expectedDelivery)))
        }
        // then
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `test deliver order`() = courierRoutesTestApplication {
        // given
        val order = orders[0]
        val accessToken = jwtTokenBuilder.accessToken(courierId, UserRole.COURIER, companyId)
        coEvery { orderRepository.deliverOrder(companyId, order.id, courierId) } just Runs
        every { getIntPathParam(any(), "orderId") } returns order.id
        // when
        val response = client.put("/courier/$courierId/order/${order.id}/delivered") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `test get order by id with non-existent order`() = courierRoutesTestApplication {
        // given
        val orderId = 999
        val accessToken = jwtTokenBuilder.accessToken(courierId, UserRole.COURIER, companyId)
        coEvery { orderRepository.getById(orderId, companyId) } returns null
        every { getIntPathParam(any(), "orderId") } returns orderId
        // when
        val response = client.get("/courier/$courierId/order/$orderId") {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}