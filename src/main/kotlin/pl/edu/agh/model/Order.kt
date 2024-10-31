package pl.edu.agh.model

import kotlinx.datetime.LocalDateTime
import pl.edu.agh.dao.OrderDAO
import java.math.BigDecimal

data class Order(
    val id: Int,
    val companyId: Int,
    val products: List<ProductEntry>,
    val client: User,
    val name: String?,
    val placedOn: LocalDateTime,
    val sendOn: LocalDateTime?,
    val deliveredOn: LocalDateTime?,
    val expectedDeliveryOn: LocalDateTime?,
    val courier: User?,
    val totalPrice: BigDecimal
) {
    constructor(orderDAO: OrderDAO) : this(
        id = orderDAO.id.value,
        companyId = orderDAO.companyDAO.id.value,
        products = orderDAO.products.map{ ProductEntry(Product(it.key), it.value) },
        client = User(orderDAO.client),
        name = orderDAO.name,
        placedOn = orderDAO.placedOn,
        sendOn = orderDAO.sendOn,
        deliveredOn = orderDAO.deliveredOn,
        expectedDeliveryOn = orderDAO.expectedDeliveryOn,
        courier = orderDAO.courier?.let { User(it) },
        totalPrice = orderDAO.totalPrice
    )
}

data class ProductEntry(
    val product: Product,
    val quantity: Int
)
