package pl.edu.agh.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.edu.agh.plugins.UserRoleAuthorizationPlugin
import pl.edu.agh.plugins.getClaimFromToken
import pl.edu.agh.repositories.ProductRepository
import pl.edu.agh.repositories.UserRepository

fun Route.commonRoutes(
    productRepository: ProductRepository,
    userRepository: UserRepository
) {
    route(Regex("/(?<userRole>(client|admin))")) {
        install(UserRoleAuthorizationPlugin)
        get("/products") {
            val companyId: Int = getIntPathParam(call, "companyId")
            val products = productRepository.getAll(companyId)
            call.respond(products)
        }

        get("/product/{productId}") {
            val productId: Int = getIntPathParam(call, "productId")
            val companyId: Int = getIntPathParam(call, "companyId")
            val product = getEntityById(productId, companyId, productRepository::getById)
            call.respond(product)
        }

        get {
            val userId = getClaimFromToken(call, "user").asInt()
            val companyId: Int = getIntPathParam(call, "companyId")
            val user = getEntityById(userId, companyId, userRepository::getById)
            call.respond(user)
        }
    }

}