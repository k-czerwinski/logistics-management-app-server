package pl.edu.agh.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import pl.edu.agh.repositories.CompanyRepository
import pl.edu.agh.repositories.OrderRepository
import pl.edu.agh.repositories.ProductRepository
import pl.edu.agh.repositories.UserRepository
import pl.edu.agh.routes.*

fun Application.configureRouting(
    jwtProperties: JwtProperties,
    userRepository: UserRepository,
    companyRepository: CompanyRepository,
    productRepository: ProductRepository,
    orderRepository: OrderRepository
) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        authorizationRoutes(userRepository, jwtProperties)
    }
    routing {
        authenticate {
            route("/company/{companyId}") {
                install(PathParamAuthorizationPlugin) {
                    pathParameterName = "companyId"
                    jwtPrincipalClaimName = "company"
                }
                userRoutes(userRepository)
                productRoutes(productRepository)
                orderRoutes(orderRepository)
            }
            companyRoutes(companyRepository)
        }
    }
}
