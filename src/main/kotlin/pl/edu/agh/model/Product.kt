package pl.edu.agh.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import pl.edu.agh.dao.ProductDAO
import pl.edu.agh.model.serializers.BigDecimalSerializer
import java.math.BigDecimal

@Serializable
data class Product(
    val id: Int,
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val description: String?,
    @Transient
    val company: Company = Company(-1, "placeholder_value_for_serialization", "placeholder_value_for_serialization", null)
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