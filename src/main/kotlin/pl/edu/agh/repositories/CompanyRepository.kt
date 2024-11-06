package pl.edu.agh.repositories

import pl.edu.agh.dao.CompanyDAO
import pl.edu.agh.dao.CompanyTable
import pl.edu.agh.model.Company
import pl.edu.agh.dto.CompanyCreateDTO

class CompanyRepository {
    suspend fun getById(entityId: Int, companyId: Int): Company? = suspendTransaction {
        require(companyId == entityId) { IllegalArgumentException("Company id must be equal to entity id") }
        CompanyDAO.findById(entityId)?.let(::Company)
    }

    suspend fun getByDomain(domain: String): Company? = suspendTransaction {
        CompanyDAO.find { CompanyTable.domain eq domain }.firstOrNull()?.let(::Company)
    }

    suspend fun add(item: CompanyCreateDTO) : Unit = suspendTransaction {
        CompanyDAO.new {
            name = item.name
            domain = item.domain
        }
    }
}