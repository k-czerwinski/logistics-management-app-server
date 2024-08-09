package pl.edu.agh.model

import kotlinx.serialization.Serializable
import pl.edu.agh.dao.CompanyDAO

@Serializable
data class Company(
    val id: Int,
    val name: String,
    val logo: CompanyLogo?
)

@Serializable
data class CompanyCreateDTO(
    val name: String,
    val logo: CompanyLogoCreateDTO? = null
)

fun toCompany(dao: CompanyDAO) = Company(
    id = dao.id.value,
    name = dao.name,
    logo = dao.logo?.let { toCompanyLogo(it) }
)