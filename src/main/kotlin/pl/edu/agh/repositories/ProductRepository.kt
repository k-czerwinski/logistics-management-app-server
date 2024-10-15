package pl.edu.agh.repositories

import org.jetbrains.exposed.sql.and
import pl.edu.agh.dao.CompanyDAO
import pl.edu.agh.dao.ProductDAO
import pl.edu.agh.dao.ProductTable
import pl.edu.agh.model.Product
import pl.edu.agh.model.ProductCreateDTO
import pl.edu.agh.model.toProduct

class ProductRepository : Repository<Product, ProductCreateDTO> {
    override suspend fun getAll(companyId: Int): List<Product> = suspendTransaction {
        ProductDAO.find { ProductTable.company eq companyId }.map(::toProduct)
    }

    override suspend fun getById(entityId: Int, companyId: Int): Product? = suspendTransaction {
        ProductDAO.find { (ProductTable.id eq entityId) and (ProductTable.company eq companyId) }.firstOrNull()
            ?.let(::toProduct)
    }

    override suspend fun add(item: ProductCreateDTO, companyId: Int): Product = suspendTransaction {
        val productDAO = ProductDAO.new {
            name = item.name
            description = item.description
            price = item.price
            companyDAO = CompanyDAO[companyId]
        }
        toProduct(productDAO)
    }

    override suspend fun update(item: Product): Product {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }
}