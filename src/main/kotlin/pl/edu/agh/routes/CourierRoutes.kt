package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.edu.agh.dto.OrderDTO
import pl.edu.agh.dto.OrderExpectedDeliveryDTO
import pl.edu.agh.dto.OrderListViewDTO
import pl.edu.agh.plugins.PathParamAuthorizationPlugin
import pl.edu.agh.plugins.UserRoleAuthorizationPlugin
import pl.edu.agh.repositories.OrderRepository
import pl.edu.agh.repositories.getEntityById

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
            val orders = orderRepository.getAllByCourierId(courierId, companyId).map(::OrderListViewDTO).toList()
            call.respond(HttpStatusCode.OK, orders)
        }

        get("/order/{orderId}") {
            val orderId: Int = getIntPathParam(call, "orderId")
            val companyId: Int = getIntPathParam(call, "companyId")
            val courierId: Int = getIntPathParam(call, "courierId")
            val order = getEntityById(orderId, companyId, orderRepository::getById).let(::OrderDTO)
            if (order.courier == null || order.courier != courierId) {
                throw NotFoundException(
                    "Order with id $orderId does not exist or is not assigned to the courier with id $courierId"
                )
            }
            call.respond(HttpStatusCode.OK, order)
        }

        put("/order/{orderId}/delivered") {
            val companyId: Int = getIntPathParam(call, "companyId")
            val orderId: Int = getIntPathParam(call, "orderId")
            val courierId: Int = getIntPathParam(call, "courierId")
            try {
                orderRepository.deliverOrder(companyId, orderId, courierId)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: Exception) {
                throw NotFoundException(
                    "Order with id $orderId does not exist, is already delivered or is not assigned to the courier with id $courierId"
                )
            }
        }

        put("/order/{orderId}/expected-delivery") {
            val companyId: Int = getIntPathParam(call, "companyId")
            val orderId: Int = getIntPathParam(call, "orderId")
            val courierId: Int = getIntPathParam(call, "courierId")
            val expectedDelivery = call.receive<OrderExpectedDeliveryDTO>().expectedDelivery
            try {
                orderRepository.setExpectedDelivery(companyId, orderId, courierId, expectedDelivery)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: Exception) {
                throw NotFoundException(
                    "Order with id $orderId does not exist, is already delivered or is not assigned to the courier with id $courierId"
                )
            }
        }
    }
}