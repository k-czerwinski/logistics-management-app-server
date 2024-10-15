package pl.edu.agh.model

import kotlinx.serialization.Serializable
import pl.edu.agh.dao.ProductDAO
import pl.edu.agh.model.serializers.BigDecimalSerializer
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
    constructor(product: ProductDAO) : this(product.id.value, product.name, product.price, product.description)
}
data class Product(
    val id: Int,
    val name: String,
    val price: BigDecimal,
    val description: String?,
    val company: Company
)

@Serializable
data class ProductCreateDTO(
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val description: String?
)

fun toProduct(dao: ProductDAO) = Product(
    id = dao.id.value,
    name = dao.name,
    price = dao.price,
    description = dao.description,
    company = toCompany(dao.companyDAO)
)