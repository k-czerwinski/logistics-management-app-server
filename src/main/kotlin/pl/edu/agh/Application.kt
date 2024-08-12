package pl.edu.agh

import io.ktor.server.application.*
import io.ktor.server.netty.*
import pl.edu.agh.plugins.JwtProperties
import pl.edu.agh.plugins.configureDatabases
import pl.edu.agh.plugins.configureRouting
import pl.edu.agh.plugins.configureSecurity
import pl.edu.agh.repositories.CompanyRepository
import pl.edu.agh.repositories.OrderRepository
import pl.edu.agh.repositories.ProductRepository
import pl.edu.agh.repositories.UserRepository

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val jwtAudience = environment.config.property("ktor.security.jwt.audience").toString()
    val jwtDomain = environment.config.propertyOrNull("ktor.security.jwt.issuer").toString()
    val jwtRealm = environment.config.propertyOrNull("ktor.security.jwt.realm").toString()
    val jwtSecret = environment.config.propertyOrNull("ktor.security.jwt.secret").toString()
    val jwtProperties = JwtProperties(jwtAudience, jwtDomain, jwtRealm, jwtSecret)

    val productRepository = ProductRepository()
    val orderRepository = OrderRepository()
    val userRepository = UserRepository()
    val companyRepository = CompanyRepository()
    configureDatabases()
    configureSecurity(jwtProperties)
    configureRouting(jwtProperties, userRepository, companyRepository, productRepository, orderRepository)
}