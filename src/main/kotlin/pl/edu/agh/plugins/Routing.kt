package pl.edu.agh.plugins

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
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
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
//                HTTP 404 is returned not to reveal user and token information
                is JwtWithoutRequiredClaimException -> call.respond(HttpStatusCode.NotFound)
                is PermissionDeniedException -> call.respond(HttpStatusCode.NotFound)
            }
        }
    }
    routing {
        authorizationRoutes(userRepository, jwtProperties)
        authenticate {
            route("/company/{companyId}") {
                install(PathParamAuthorizationPlugin) {
                    pathParameterName = "companyId"
                    jwtPrincipalClaimName = "company"
                }
                clientRoutes(orderRepository)
                adminRoutes(productRepository, userRepository, orderRepository)
                commonRoutes(productRepository, userRepository)
                get {
                    val companyId: Int = getIntPathParam(call, "companyId")
                    val company = getEntityById(companyId, companyId, companyRepository::getById)
                    call.respond(company)
                }
            }
        }
    }
}
