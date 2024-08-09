package pl.edu.agh.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import pl.edu.agh.repositories.CompanyRepository
import pl.edu.agh.repositories.OrderRepository
import pl.edu.agh.repositories.ProductRepository
import pl.edu.agh.repositories.UserRepository
import pl.edu.agh.routes.companyRoutes
import pl.edu.agh.routes.orderRoutes
import pl.edu.agh.routes.productRoutes
import pl.edu.agh.routes.userRoutes

fun Application.configureRouting(userRepository: UserRepository,
                                 companyRepository: CompanyRepository,
                                 productRepository: ProductRepository,
                                 orderRepository: OrderRepository
) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        userRoutes(userRepository)
        companyRoutes(companyRepository)
        productRoutes(productRepository)
        orderRoutes(orderRepository)
    }
}
