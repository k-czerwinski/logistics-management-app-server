package pl.edu.agh.repositories

import pl.edu.agh.dao.CompanyDAO
import pl.edu.agh.dao.CompanyLogoDAO
import pl.edu.agh.model.Company
import pl.edu.agh.model.CompanyCreateDTO
import pl.edu.agh.model.toCompany

class CompanyRepository : Repository<Company, CompanyCreateDTO> {
    override suspend fun getAll(): List<Company> = suspendTransaction {
        CompanyDAO.all().map(::toCompany)
    }

    override suspend fun getById(id: Int): Company? = suspendTransaction {
        CompanyDAO.findById(id)?.let(::toCompany)
    }

//    if logo is not null then it is added to the database automatically with company
    override suspend fun add(item: CompanyCreateDTO) : Unit = suspendTransaction {
        CompanyDAO.new {
            name = item.name
            logo = item.logo?.let { CompanyLogoDAO.new { image = item.logo.image } }
        }
    }

    override suspend fun update(item: Company): Company {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }
}