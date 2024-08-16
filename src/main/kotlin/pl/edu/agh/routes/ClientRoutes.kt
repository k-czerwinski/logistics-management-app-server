package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.edu.agh.model.OrderCreateDTO
import pl.edu.agh.plugins.PathParamAuthorizationPlugin
import pl.edu.agh.plugins.UserRoleAuthorizationPlugin
import pl.edu.agh.repositories.OrderRepository

fun Route.clientRoutes(orderRepository: OrderRepository) {
    route(Regex("/(?<userRole>client)/(?<client>\\d+)")) {
        install(UserRoleAuthorizationPlugin)
        install(PathParamAuthorizationPlugin) {
            pathParameterName = "clientId"
            jwtPrincipalClaimName = "user"
        }
        post("/order") {
            val order = call.receive<OrderCreateDTO>()
            validateWithPathParam(call, order.clientId, "clientId")
            validateWithPathParam(call, order.companyId, "companyId")
            try {
                orderRepository.add(order)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Company with id ${order.companyId} does not exist")
                return@post
            }
            call.respond(HttpStatusCode.Created)
        }

        get("/order/{orderId}") {
            val orderId: Int = getIntPathParam(call, "orderId")
            val companyId: Int = getIntPathParam(call, "companyId")
            val order = getEntityById(orderId, companyId, orderRepository::getById)
            if(order.client.id != getIntPathParam(call, "clientId")) {
                throw NotFoundException("Order with id $orderId does not exist")
            }
            call.respond(order)
        }
    }
}