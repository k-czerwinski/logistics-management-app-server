package pl.edu.agh.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object CompanyTable: IntIdTable() {
    val name = varchar("name", 30)
    val logo = reference("logo_id", CompanyLogoTable, fkName = "FK_Company_Logo_Id").nullable()
}

object CompanyLogoTable: IntIdTable("company_logo") {
    val image = binary("image")
}

class CompanyDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CompanyDAO>(CompanyTable)

    var name by CompanyTable.name
    var logo by CompanyLogoDAO optionalReferencedOn CompanyTable.logo
//    val users by User referrersOn UserTable.company
//    val products by Product referrersOn ProductTable.company
//    val orders by Order referrersOn OrderTable.company
}

class CompanyLogoDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CompanyLogoDAO>(CompanyLogoTable)

    var image by CompanyLogoTable.image
}