package pl.edu.agh.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.edu.agh.dao.OrderDAO
import pl.edu.agh.model.serializers.BigDecimalSerializer
import pl.edu.agh.model.serializers.UserToIdSerializer
import java.math.BigDecimal

@Serializable
data class Order(
    val id: Int,
    val companyId: Int,
    val products: List<ProductEntry>,
    @Serializable(with = UserToIdSerializer::class)
    val client: User,
    val name: String?,
    val placedOn: LocalDateTime,
    val sendOn: LocalDateTime?,
    val deliveredOn: LocalDateTime?,
    val expectedDeliveryOn: LocalDateTime?,
    @Serializable(with = UserToIdSerializer::class)
    val courier: User?,
    @Serializable(with = BigDecimalSerializer::class)
    val totalPrice: BigDecimal
) {

    fun toOrderListView() : OrderListView = OrderListView(
        id = id,
        companyId = companyId,
        client = client,
        name = name,
        placedOn = placedOn,
        sendOn = sendOn,
        deliveredOn = deliveredOn,
        expectedDeliveryOn = expectedDeliveryOn,
        totalPrice = totalPrice
    )
}

@Serializable
data class ProductEntry(
    val product: Product,
    val quantity: Int
)

@Serializable
data class OrderListView(
    val id: Int,
    val companyId: Int,
    @Serializable(with = UserToIdSerializer::class)
    @SerialName("clientId")
    val client: User,
    val name: String?,
    val placedOn: LocalDateTime,
    val sendOn: LocalDateTime?,
    val deliveredOn: LocalDateTime?,
    val expectedDeliveryOn: LocalDateTime?,
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
    companyId = dao.companyDAO.id.value,
    products = dao.products.map{ ProductEntry(toProduct(it.key), it.value) },
    client = toUser(dao.client),
    name = dao.name,
    placedOn = dao.placedOn,
    sendOn = dao.sendOn,
    deliveredOn = dao.deliveredOn,
    expectedDeliveryOn = dao.expectedDeliveryOn,
    courier = dao.courier?.let { toUser(it) },
    totalPrice = dao.totalPrice
)

