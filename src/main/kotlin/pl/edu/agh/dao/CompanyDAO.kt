package pl.edu.agh.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object CompanyTable: IntIdTable() {
    val name = varchar("name", 30)
    val domain = varchar("domain", 30).uniqueIndex("CONSTRAINT_UNIQUE_COMPANY_DOMAIN")
    val logo = reference("logo_id", CompanyLogoTable, fkName = "FK_Company_Logo_Id").nullable()
}

object CompanyLogoTable: IntIdTable("company_logo") {
    val image = binary("image")
}

class CompanyDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CompanyDAO>(CompanyTable)

    var name by CompanyTable.name
    var domain by CompanyTable.domain
    var logo by CompanyLogoDAO optionalReferencedOn CompanyTable.logo

}

class CompanyLogoDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CompanyLogoDAO>(CompanyLogoTable)

    var image by CompanyLogoTable.image
}