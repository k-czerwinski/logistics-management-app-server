package pl.edu.agh.model

import kotlinx.serialization.Serializable
import pl.edu.agh.dao.CompanyDAO

@Serializable
data class Company(
    val id: Int,
    val name: String,
    val domain: String,
) {
    constructor(dao: CompanyDAO) : this(dao.id.value, dao.name, dao.domain)
}
