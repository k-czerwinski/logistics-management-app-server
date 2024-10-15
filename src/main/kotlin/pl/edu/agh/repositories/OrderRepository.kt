package pl.edu.agh.repositories

import io.ktor.server.plugins.*
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.and
import pl.edu.agh.dao.*
import pl.edu.agh.model.*
import java.time.LocalDateTime

class OrderRepository : Repository<Order, OrderCreateDTO> {
    override suspend fun getAll(companyId: Int): List<Order> = suspendTransaction {
        OrderDAO.find { OrderTable.company eq companyId }.map(::toOrder)
    }

    suspend fun getAllByClientId(clientId: Int, companyId: Int): List<Order> = suspendTransaction {
        OrderDAO.find { (OrderTable.client eq clientId) and (OrderTable.company eq companyId) }
            .map(::toOrder)
    }

    suspend fun getAllByCourierId(courierId: Int, companyId: Int): List<Order> = suspendTransaction {
        OrderDAO.find { (OrderTable.courier eq courierId) and (OrderTable.company eq companyId) }
            .map(::toOrder)
    }

    override suspend fun getById(entityId: Int, companyId: Int): Order? = suspendTransaction {
        OrderDAO.find { (OrderTable.id eq entityId) and (OrderTable.company eq companyId) }.firstOrNull()
            ?.let(::toOrder)
    }

    //    it also adds all the products to the order_products table
    override suspend fun add(orderCreateDTO: OrderCreateDTO, companyId: Int): Order = suspendTransaction {
        val productDAOs = orderCreateDTO.products.map {
            ProductDAO[it.productId] to it.quantity
        }
        val totalProductsPrice = productDAOs.map { (product, quantity) ->
            product.price * quantity.toBigDecimal()
        }.reduce { acc, bigDecimal -> acc + bigDecimal }

        val orderDAO = OrderDAO.new {
            client = UserDAO[orderCreateDTO.clientId]
            placedOn = LocalDateTime.now().toKotlinLocalDateTime()
            companyDAO = CompanyDAO[companyId]
            totalPrice = totalProductsPrice
            name = orderCreateDTO.name
            sendOn = null
            deliveredOn = null
            expectedDeliveryOn = null
            courier = null
        }
        productDAOs.forEach {
            OrderProductDAO.new {
                productDAO = it.first
                quantity = it.second
                this.orderDAO = orderDAO
            }
        }
        toOrder(orderDAO)
    }

    suspend fun deliverOrder(companyId: Int, orderId: Int, courierId: Int) = suspendTransaction {
        OrderDAO.find { (OrderTable.id eq orderId) and (OrderTable.company eq companyId) and (OrderTable.courier eq courierId) and (OrderTable.deliveredOn eq null) }
            .firstOrNull()?.let {
                it.deliveredOn = LocalDateTime.now().toKotlinLocalDateTime()
            }
            ?: throw IllegalArgumentException("Order with id $orderId does not exist or is not assigned to the courier with id $courierId")
    }

    suspend fun setExpectedDelivery(companyId: Int, orderId: Int, courierId: Int, expectedDelivery: LocalDateTime) = suspendTransaction {
        OrderDAO.find { (OrderTable.id eq orderId) and (OrderTable.company eq companyId) and (OrderTable.courier eq courierId) and (OrderTable.deliveredOn eq null) }
            .firstOrNull()?.let {
                it.expectedDeliveryOn = expectedDelivery.toKotlinLocalDateTime()
            }
            ?: throw IllegalArgumentException("Order with id $orderId does not exist or is not assigned to the courier with id $courierId")
    }

    suspend fun sendOrder(companyId: Int, orderId: Int, courierId: Int) = suspendTransaction {
        val courier: UserDAO = UserDAO.find { (UserTable.id eq courierId) and (UserTable.role eq UserRole.COURIER) }
            .firstOrNull() ?: throw NotFoundException()
        OrderDAO.find { (OrderTable.id eq orderId) and (OrderTable.company eq companyId) and (OrderTable.sendOn eq null)}
            .firstOrNull()?.let {
                it.courier = courier
                it.sendOn = LocalDateTime.now().toKotlinLocalDateTime()
            } ?: throw NotFoundException("Order with id $orderId cannot be send because it does not exist or has illegal state")
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun update(item: Order): Order {
        TODO("Not yet implemented")
    }
}