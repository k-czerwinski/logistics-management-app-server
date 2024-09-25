package pl.edu.agh.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object RefreshTokenTable : IdTable<Int>("refresh_token") {
    override val id = reference("user", UserTable, fkName = "FK_RefreshToken_User_Id")
    val token = varchar("token", 300)
    val expiryDate = datetime("expiry_date")

    override val primaryKey = PrimaryKey(id, name = "PK_RefreshToken_User")
}

class RefreshTokenDAO(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, RefreshTokenDAO>(RefreshTokenTable)

    var user by UserDAO referencedOn RefreshTokenTable.id
    var token by RefreshTokenTable.token
    var expiryDate by RefreshTokenTable.expiryDate
}