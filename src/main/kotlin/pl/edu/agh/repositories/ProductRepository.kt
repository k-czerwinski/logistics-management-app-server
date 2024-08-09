package pl.edu.agh.repositories

import pl.edu.agh.dao.CompanyDAO
import pl.edu.agh.dao.ProductDAO
import pl.edu.agh.model.Product
import pl.edu.agh.model.ProductCreateDTO
import pl.edu.agh.model.toProduct

class ProductRepository : Repository<Product, ProductCreateDTO> {
    override suspend fun getAll(): List<Product>  = suspendTransaction { ProductDAO.all().map(::toProduct) }

    override suspend fun getById(id: Int): Product? = suspendTransaction {
        ProductDAO.findById(id)?.let(::toProduct)
    }

    override suspend fun add(item: ProductCreateDTO): Unit = suspendTransaction{
        ProductDAO.new {
            name = item.name
            description = item.description
            price = item.price
            companyDAO = CompanyDAO[item.companyId]
        }
    }

    override suspend fun update(item: Product): Product {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }
}