package pl.edu.agh.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object ProductTable: IntIdTable() {
    val name = varchar("name", 20)
    val price = decimal("price", 10, 2)
    val description = varchar("description", 150).nullable()
    val company = reference("company", CompanyTable, fkName = "FK_Product_Company_Id")
}

class ProductDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProductDAO>(ProductTable)

    var name by ProductTable.name
    var price by ProductTable.price
    var description by ProductTable.description
    var companyDAO by CompanyDAO referencedOn ProductTable.company
}