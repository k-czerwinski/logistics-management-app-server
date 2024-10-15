package pl.edu.agh.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object CompanyTable: IntIdTable() {
    val name = varchar("name", 30)
    val domain = varchar("domain", 30).uniqueIndex("CONSTRAINT_UNIQUE_COMPANY_DOMAIN")
}

class CompanyDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CompanyDAO>(CompanyTable)

    var name by CompanyTable.name
    var domain by CompanyTable.domain
}
