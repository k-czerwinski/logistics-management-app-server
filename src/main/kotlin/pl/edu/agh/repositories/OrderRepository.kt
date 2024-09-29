package pl.edu.agh.repositories

import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.and
import pl.edu.agh.dao.*
import pl.edu.agh.model.Order
import pl.edu.agh.model.OrderCreateDTO
import pl.edu.agh.model.toOrder
import java.time.LocalDateTime

class OrderRepository : Repository<Order, OrderCreateDTO> {
    override suspend fun getAll(companyId: Int): List<Order> = suspendTransaction {
        OrderDAO.find{ OrderTable.company eq companyId }.map(::toOrder)
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
    override suspend fun add(orderCreateDTO: OrderCreateDTO): Order = suspendTransaction {
        val productDAOs = orderCreateDTO.products.map {
            ProductDAO[it.productId] to it.quantity
        }
        val totalProductsPrice = productDAOs.map { (product, quantity) ->
            product.price * quantity.toBigDecimal()
        }.reduce { acc, bigDecimal -> acc + bigDecimal }

        val orderDAO = OrderDAO.new {
            client = UserDAO[orderCreateDTO.clientId]
            placedOn = LocalDateTime.now().toKotlinLocalDateTime()
            companyDAO = CompanyDAO[orderCreateDTO.companyId]
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

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun update(item: Order): Order {
        TODO("Not yet implemented")
    }
}