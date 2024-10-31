package pl.edu.agh.repositories

import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.BeforeClass
import org.junit.Test
import pl.edu.agh.dao.CompanyDAO
import pl.edu.agh.dao.CompanyTable
import pl.edu.agh.dao.OrderDAO
import pl.edu.agh.dao.OrderProductDAO
import pl.edu.agh.dao.OrderProductTable
import pl.edu.agh.dao.OrderTable
import pl.edu.agh.dao.ProductDAO
import pl.edu.agh.dao.ProductTable
import pl.edu.agh.dao.UserDAO
import pl.edu.agh.dao.UserTable
import pl.edu.agh.dto.OrderCreateDTO
import pl.edu.agh.dto.OrderProductCreateDTO
import pl.edu.agh.model.Company
import pl.edu.agh.model.Order
import pl.edu.agh.model.Product
import pl.edu.agh.model.ProductEntry
import pl.edu.agh.model.User
import pl.edu.agh.model.UserRole
import java.math.BigDecimal
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OrderRepositoryTest {
    companion object {
        lateinit var orderRepository: OrderRepository

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
                SchemaUtils.create(CompanyTable, ProductTable, OrderProductTable, OrderTable, UserTable)
            }
            orderRepository = OrderRepository()
        }
    }

    val companies = listOf(
        Company(1, "Company 1", "sample-company.com"),
        Company(2, "Company 2", "another-company.com")
    )
    val company = companies[0]
    val user = User(1, "FirstName", "LastName", "username1", "password123###", UserRole.CLIENT, company)
    val courier = User(2, "FirstName", "LastName", "username2", "password123##", UserRole.COURIER, company)
    val products = listOf(
        Product(1, "Product 1", BigDecimal(12), "Description 1", company),
        Product(2, "Product 2", BigDecimal(13), "Description 2", company),
        Product(3, "Product 3", BigDecimal(30), "Description 3", company),
        Product(4, "Product 4", BigDecimal(50), "Description 4", company)
    )
    val orders = listOf(
        Order(
            id = 1,
            companyId = company.id,
            products = products.shuffled().take(2).map { ProductEntry(it, 10) },
            client = user,
            name = "Order 1",
            placedOn = LocalDateTime(2024, 10, 1, 10, 0),
            sendOn = LocalDateTime(2024, 10, 2, 10, 0),
            deliveredOn = LocalDateTime(2024, 10, 3, 10, 0),
            expectedDeliveryOn = LocalDateTime(2024, 10, 3, 10, 0),
            courier = courier,
            totalPrice = BigDecimal(40.00).setScale(2)
        ),
        Order(
            id = 2,
            companyId = company.id,
            products = products.shuffled().take(1).map { ProductEntry(it, 10) },
            client = user,
            name = "Order 1",
            placedOn = LocalDateTime(2024, 10, 1, 10, 0),
            sendOn = LocalDateTime(2024, 10, 2, 10, 0),
            deliveredOn = LocalDateTime(2024, 10, 3, 10, 0),
            expectedDeliveryOn = LocalDateTime(2024, 10, 3, 10, 0),
            courier = courier,
            totalPrice = BigDecimal(40.00).setScale(2)
        ),
        Order(
            id = 3,
            companyId = company.id,
            products = products.map { ProductEntry(it, 10) },
            client = user,
            name = "Order 1",
            placedOn = LocalDateTime(2024, 10, 1, 10, 0),
            sendOn = LocalDateTime(2024, 10, 2, 10, 0),
            deliveredOn = LocalDateTime(2024, 10, 3, 10, 0),
            expectedDeliveryOn = LocalDateTime(2024, 10, 3, 10, 0),
            courier = courier,
            totalPrice = BigDecimal(40.00).setScale(2)
        ),
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
            UserDAO.new(user.id) {
                username = user.username
                password = user.password
                firstName = user.firstName
                lastName = user.lastName
                companyDAO = CompanyDAO[user.company.id]
                role = user.role
            }
            UserDAO.new(courier.id) {
                username = courier.username
                password = courier.password
                firstName = courier.firstName
                lastName = courier.lastName
                companyDAO = CompanyDAO[courier.company.id]
                role = courier.role
            }
            products.forEach {
                ProductDAO.new(it.id) {
                    name = it.name
                    description = it.description
                    price = it.price
                    companyDAO = CompanyDAO[it.company.id]
                }
            }
            orders.forEach {
                val orderDAO = OrderDAO.new(it.id) {
                    client = UserDAO[it.client.id]
                    placedOn = it.placedOn
                    companyDAO = CompanyDAO[it.companyId]
                    totalPrice = it.totalPrice
                    name = it.name
                    sendOn = it.sendOn
                    deliveredOn = it.deliveredOn
                    expectedDeliveryOn = it.expectedDeliveryOn
                    courier = it.courier?.id?.let { UserDAO[it] }
                }
                it.products.forEach {
                    OrderProductDAO.new {
                        productDAO = ProductDAO[it.product.id]
                        quantity = it.quantity
                        this.orderDAO = orderDAO
                    }
                }
            }

        }
    }

    @AfterTest
    fun tearDown() {
        transaction {
            OrderProductDAO.all().forEach { it.delete() }
            OrderDAO.all().forEach { it.delete() }
            UserDAO.all().forEach { it.delete() }
            ProductDAO.all().forEach { it.delete() }
            CompanyDAO.all().forEach { it.delete() }
        }
    }

    @Test
    fun `test getAll returns all orders for a company`() = runTest {
        // given
        val companyId = company.id

        // when
        val result = orderRepository.getAll(companyId).sortedBy(Order::id)

        // then
        assertContentEquals(orders, result)
    }

    @Test
    fun `test getById returns order when found`() = runTest {
        // given
        val order = orders[0]

        // when
        val result = orderRepository.getById(order.id, order.companyId)

        // then
        assertEquals(order, result)
    }

    @Test
    fun `test getById returns null when order not found`() = runTest {
        // given
        val orderId = -1
        val companyId = company.id

        // when
        val result = orderRepository.getById(orderId, companyId)

        // then
        assertNull(result)
    }

    @Test
    fun `test add creates new order`() = runTest {
        // given
        transaction {
            OrderProductDAO.all().forEach(OrderProductDAO::delete)
            OrderDAO.all().forEach(OrderDAO::delete)
        }
        val orderDTO = OrderCreateDTO(listOf(OrderProductCreateDTO(products[0].id, 5)), user.id, "Order 1")
        val companyId = company.id

        // when
        orderRepository.add(orderDTO, companyId)

        // then
        val order = transaction {
            OrderDAO.find { OrderTable.name eq orderDTO.name }.firstOrNull()
        }
        assertNotNull(order)
        assertEquals(orderDTO.name, order.name)
    }
}
