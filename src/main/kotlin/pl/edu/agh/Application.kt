package pl.edu.agh

import io.ktor.server.application.*
import io.ktor.server.netty.*
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
    val productRepository = ProductRepository()
    val orderRepository = OrderRepository()
    val userRepository = UserRepository()
    val companyRepository = CompanyRepository()
    configureSecurity()
    configureDatabases()
    configureRouting(userRepository, companyRepository, productRepository, orderRepository)
}