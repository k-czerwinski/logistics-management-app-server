package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.edu.agh.model.Order
import pl.edu.agh.plugins.PathParamAuthorizationPlugin
import pl.edu.agh.plugins.UserRoleAuthorizationPlugin
import pl.edu.agh.repositories.OrderRepository

fun Route.courierRoutes(orderRepository: OrderRepository) {
    route(Regex("/(?<userRole>courier)/(?<courierId>\\d+)")) {
        install(UserRoleAuthorizationPlugin)
        install(PathParamAuthorizationPlugin("courierIdPathValidationPlugin")) {
            pathParameterName = "courierId"
            jwtPrincipalClaimName = "user"
        }

        get("/orders") {
            val companyId: Int = getIntPathParam(call, "companyId")
            val courierId: Int = getIntPathParam(call, "courierId")
            val orders = orderRepository.getAllByCourierId(courierId, companyId).map(Order::toOrderListView).toList()
            call.respond(HttpStatusCode.OK, orders)
        }

        get("/order/{orderId}") {
            val orderId: Int = getIntPathParam(call, "orderId")
            val companyId: Int = getIntPathParam(call, "companyId")
            val order = getEntityById(orderId, companyId, orderRepository::getById)
            if (order.courier == null || order.courier.id != getIntPathParam(call, "courierId")) {
                throw NotFoundException(
                    "Order with id $orderId does not exist or is not assigned to the courier with id ${getIntPathParam(call, "courierId")}"
                )
            }
            call.respond(HttpStatusCode.OK, order)
        }
    }
}