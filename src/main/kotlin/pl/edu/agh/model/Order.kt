package pl.edu.agh.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import pl.edu.agh.dao.OrderDAO
import pl.edu.agh.model.serializers.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class Order(
    val id: Int,
    val company: Company,
//    TODO add serializer for Map
    val products: Map<Product, Int>,
    val client: User,
    val name: String?,
    val placedOn: LocalDateTime,
    val sendOn: LocalDateTime?,
    val deliveredOn: LocalDateTime?,
    val expectedDeliveryOn: LocalDateTime?,
    val courier: User?,
    @Serializable(with = BigDecimalSerializer::class)
    val totalPrice: BigDecimal
)

@Serializable
data class OrderCreateDTO(
    val companyId: Int,
    val products: List<OrderProductCreateDTO>,
    val clientId: Int,
    val name: String?,
    val sendOn: LocalDateTime? = null,
    val deliveredOn: LocalDateTime? = null,
    val expectedDeliveryOn: LocalDateTime? = null,
    val courierId: Int? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val totalPrice: BigDecimal
)

@Serializable
data class OrderProductCreateDTO(
    val productId: Int,
    val quantity: Int
)

fun toOrder(dao: OrderDAO) = Order(
    id = dao.id.value,
    company = toCompany(dao.companyDAO),
    products = dao.products.mapKeys { toProduct(it.key) },
    client = toUser(dao.client),
    name = dao.name,
    placedOn = dao.placedOn,
    sendOn = dao.sendOn,
    deliveredOn = dao.deliveredOn,
    expectedDeliveryOn = dao.expectedDeliveryOn,
    courier = dao.courier?.let { toUser(it) },
    totalPrice = dao.totalPrice
)

