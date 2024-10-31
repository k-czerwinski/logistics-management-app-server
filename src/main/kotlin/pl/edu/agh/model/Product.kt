package pl.edu.agh.model

import pl.edu.agh.dao.ProductDAO
import java.math.BigDecimal

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
}
