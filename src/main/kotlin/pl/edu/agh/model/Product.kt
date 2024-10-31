package pl.edu.agh.model

import pl.edu.agh.dao.ProductDAO
import java.math.BigDecimal
import java.math.RoundingMode

data class Product(
    val id: Int,
    val name: String,
    val price: BigDecimal,
    val description: String?,
    val company: Company
) {
    constructor(productDAO: ProductDAO) : this(
        id = productDAO.id.value,
        name = productDAO.name,
        price = productDAO.price,
        description = productDAO.description,
        company = Company(productDAO.companyDAO)
    )

    private fun compareBigDecimalsWithPrecision(value1: BigDecimal, value2: BigDecimal): Boolean {
        val scaledValue1 = value1.setScale(2, RoundingMode.DOWN)
        val scaledValue2 = value2.setScale(2, RoundingMode.DOWN)
        return scaledValue1 == scaledValue2
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Product) return false

        return id == other.id &&
                name == other.name &&
                compareBigDecimalsWithPrecision(price, other.price) &&
                description == other.description &&
                company == other.company
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + price.setScale(2, RoundingMode.DOWN).hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + company.hashCode()
        return result
    }
}
