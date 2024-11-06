package pl.edu.agh.plugins

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.edu.agh.repositories.*
import pl.edu.agh.routes.*

fun Application.configureRouting(
    jwtTokenBuilder: JwtTokenBuilder,
    userRepository: UserRepository,
    companyRepository: CompanyRepository,
    productRepository: ProductRepository,
    orderRepository: OrderRepository,
    refreshTokenRepository: RefreshTokenRepository
) {
    install(ContentNegotiation) {
        json()
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
//                HTTP 404 is returned not to reveal user and token information
                is JwtWithoutRequiredClaimException -> call.respond(HttpStatusCode.NotFound)
                is PermissionDeniedException -> call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
    routing {
        authRoutes(userRepository, companyRepository, refreshTokenRepository, jwtTokenBuilder)
        authenticate {
            route("/company/{companyId}") {
                install(PathParamAuthorizationPlugin("companyIdPathValidationPlugin")) {
                    pathParameterName = "companyId"
                    jwtPrincipalClaimName = "company"
                }
                clientRoutes(orderRepository)
                courierRoutes(orderRepository)
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
