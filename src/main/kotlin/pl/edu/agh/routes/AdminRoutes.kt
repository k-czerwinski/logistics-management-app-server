package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.edu.agh.model.ProductCreateDTO
import pl.edu.agh.model.UserCreateDTO
import pl.edu.agh.plugins.UserRoleAuthorizationPlugin
import pl.edu.agh.repositories.OrderRepository
import pl.edu.agh.repositories.ProductRepository
import pl.edu.agh.repositories.UserRepository

fun Route.adminRoutes(
    productRepository: ProductRepository,
    userRepository: UserRepository,
    orderRepository: OrderRepository
) {
    route(Regex("/(?<userRole>admin)")) {
        install(UserRoleAuthorizationPlugin)
        post("/product") {
            val product = call.receive<ProductCreateDTO>()
            validateWithPathParam(call, product.companyId, "companyId")
            try {
                productRepository.add(product)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            call.respond(HttpStatusCode.Created)
        }

        post("/user") {
            val user = call.receive<UserCreateDTO>()
            validateWithPathParam(call, user.companyId, "companyId")
            userRepository.getByUsername(user.username, user.companyId)?.let {
                if (it.username == user.username) {
                    call.respond(HttpStatusCode.Conflict, "User with username ${user.username} already exists")
                    return@post
                }
            }
            try {
                userRepository.add(user)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.UnprocessableEntity)
                return@post
            }
            call.respond(HttpStatusCode.Created)
        }

        get("/order/{orderId}") {
            val orderId: Int = getIntPathParam(call, "orderId")
            val companyId: Int = getIntPathParam(call, "companyId")
            val order = getEntityById(orderId, companyId, orderRepository::getById)
            call.respond(order)
        }
    }
}