package pl.edu.agh.repositories

import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.and
import pl.edu.agh.dao.*
import pl.edu.agh.model.Order
import pl.edu.agh.model.OrderCreateDTO
import pl.edu.agh.model.toOrder
import java.time.LocalDateTime

class OrderRepository : Repository<Order, OrderCreateDTO> {
    override suspend fun getAll(): List<Order> = suspendTransaction {
        OrderDAO.all().map(::toOrder)
    }

    override suspend fun getById(entityId: Int, companyId: Int): Order? = suspendTransaction {
        OrderDAO.find { (OrderTable.id eq entityId) and (OrderTable.company eq companyId) }.firstOrNull()
            ?.let(::toOrder)
    }

    //    it also adds all the products to the order_products table
    override suspend fun add(item: OrderCreateDTO): Unit = suspendTransaction {
        val orderDAO = OrderDAO.new {
            client = UserDAO[item.clientId]
            placedOn = LocalDateTime.now().toKotlinLocalDateTime()
            companyDAO = CompanyDAO[item.companyId]
            totalPrice = item.totalPrice
            name = item.name
            sendOn = item.sendOn
            deliveredOn = item.deliveredOn
            expectedDeliveryOn = item.expectedDeliveryOn
            courier = item.courierId?.let { UserDAO[it] }
        }
        item.products.forEach {
            OrderProductDAO.new {
                productDAO = ProductDAO[it.productId]
                quantity = it.quantity
                this.orderDAO = orderDAO
            }
        }
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun update(item: Order): Order {
        TODO("Not yet implemented")
    }
}