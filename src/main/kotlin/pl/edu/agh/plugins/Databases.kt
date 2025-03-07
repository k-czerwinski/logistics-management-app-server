package pl.edu.agh.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import pl.edu.agh.dao.*

fun Application.configureDatabases() {
    val url = environment.config.property("ktor.database.url").getString()
    val user = environment.config.property("ktor.database.user").getString()
    val password = environment.config.property("ktor.database.password").getString()
    val driver = environment.config.property("ktor.database.driver").getString()

    Database.connect(url, driver, user, password)

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(CompanyTable, ProductTable, OrderProductTable, OrderTable, UserTable, RefreshTokenTable)
    }
}
