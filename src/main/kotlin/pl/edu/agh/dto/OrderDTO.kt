package pl.edu.agh.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import pl.edu.agh.model.Order
import pl.edu.agh.dto.serializers.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class OrderDTO(
    val id: Int,
    val companyId: Int,
    val products: List<ProductEntryDTO>,
    val client: Int,
    val name: String?,
    val placedOn: LocalDateTime,
    val sendOn: LocalDateTime?,
    val deliveredOn: LocalDateTime?,
    val expectedDeliveryOn: LocalDateTime?,
    val courier: Int?,
    @Serializable(with = BigDecimalSerializer::class)
    val totalPrice: BigDecimal
) {
    constructor(order: Order) : this(
        id = order.id,
        companyId = order.companyId,
        products = order.products.map { ProductEntryDTO(ProductDTO(it.product), it.quantity) },
        client = order.client.id,
        name = order.name,
        placedOn = order.placedOn,
        sendOn = order.sendOn,
        deliveredOn = order.deliveredOn,
        expectedDeliveryOn = order.expectedDeliveryOn,
        courier = order.courier?.id,
        totalPrice = order.totalPrice
    )
}

@Serializable
data class ProductEntryDTO(
    val product: ProductDTO,
    val quantity: Int
)

@Serializable
data class OrderListViewDTO(
    val id: Int,
    val companyId: Int,
    val clientId: Int,
    val name: String?,
    val placedOn: LocalDateTime,
    val sendOn: LocalDateTime?,
    val deliveredOn: LocalDateTime?,
    val expectedDeliveryOn: LocalDateTime?,
    @Serializable(with = BigDecimalSerializer::class)
    val totalPrice: BigDecimal
) {
    constructor(order: Order) : this(
        id = order.id,
        companyId = order.companyId,
        clientId = order.client.id,
        name = order.name,
        placedOn = order.placedOn,
        sendOn = order.sendOn,
        deliveredOn = order.deliveredOn,
        expectedDeliveryOn = order.expectedDeliveryOn,
        totalPrice = order.totalPrice
    )
}

@Serializable
data class OrderCreateDTO(
    val products: List<OrderProductCreateDTO>,
    val clientId: Int,
    val name: String?,
)

@Serializable
data class OrderProductCreateDTO(
    val productId: Int,
    val quantity: Int
)

@Serializable
data class OrderCreateResponseDTO(
    @Serializable(with = BigDecimalSerializer::class)
    val totalPrice: BigDecimal
)

@Serializable
data class OrderExpectedDeliveryDTO(
    val expectedDelivery: LocalDateTime
)
