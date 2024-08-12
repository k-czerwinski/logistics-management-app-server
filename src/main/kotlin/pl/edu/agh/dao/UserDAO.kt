package pl.edu.agh.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.charLength
import pl.edu.agh.model.UserRole

object UserTable: IntIdTable() {
    val username = varchar("username", 40)
    val password = varchar("password", 100)
    val firstName = varchar("first_name", 40)
    val lastName = varchar("last_name", 40)
    val company = reference("company", CompanyTable, fkName = "FK_User_Company_Id")
    val role = enumeration("role", UserRole::class)
    val temporaryPassword = bool("temporary_password")
    init {
        check("password_min_length") { password.charLength() greaterEq 8 }
        uniqueIndex("unique_username_company", username, company)
    }
}

class UserDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDAO>(UserTable)

    var username by UserTable.username
    var password by UserTable.password
    var firstName by UserTable.firstName
    var lastName by UserTable.lastName
    var companyDAO by CompanyDAO referencedOn UserTable.company
    var role by UserTable.role
    var temporaryPassword by UserTable.temporaryPassword
//    val client by Order referrersOn OrderTable.client
//    val courier by Order optionalReferrersOn OrderTable.courier
}
