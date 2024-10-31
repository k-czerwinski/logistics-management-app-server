package pl.edu.agh.dto

import kotlinx.serialization.Serializable
import pl.edu.agh.model.Product
import pl.edu.agh.dto.serializers.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class ProductDTO(
    val id: Int,
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val description: String?
) {
    constructor(product: Product) : this(product.id, product.name, product.price, product.description)
}

@Serializable
data class ProductCreateDTO(
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val description: String?
)