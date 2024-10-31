package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.edu.agh.dto.OrderCreateDTO
import pl.edu.agh.dto.OrderCreateResponseDTO
import pl.edu.agh.dto.OrderDTO
import pl.edu.agh.dto.OrderListViewDTO
import pl.edu.agh.plugins.PathParamAuthorizationPlugin
import pl.edu.agh.plugins.UserRoleAuthorizationPlugin
import pl.edu.agh.repositories.OrderRepository
import pl.edu.agh.repositories.getEntityById

fun Route.clientRoutes(orderRepository: OrderRepository) {
    route(Regex("/(?<userRole>client)/(?<clientId>\\d+)")) {
        install(UserRoleAuthorizationPlugin)
        install(PathParamAuthorizationPlugin("clientIdPathValidationPlugin")) {
            pathParameterName = "clientId"
            jwtPrincipalClaimName = "user"
        }
        post("/order") {
            val orderCreateDTO = call.receive<OrderCreateDTO>()
            validateWithPathParam(call, orderCreateDTO.clientId, "clientId")
            val companyId = getIntPathParam(call, "companyId")
            try {
                val order = orderRepository.add(orderCreateDTO, companyId)
                call.respond(HttpStatusCode.Created, OrderCreateResponseDTO(order.totalPrice))
                return@post
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, "Company with id $companyId does not exist")
                return@post
            }
        }

        get("/orders") {
            val companyId: Int = getIntPathParam(call, "companyId")
            val clientId: Int = getIntPathParam(call, "clientId")
            val orders = orderRepository.getAllByClientId(clientId, companyId).map(::OrderListViewDTO).toList()
            call.respond(HttpStatusCode.OK, orders)
        }

        get("/order/{orderId}") {
            val orderId: Int = getIntPathParam(call, "orderId")
            val companyId: Int = getIntPathParam(call, "companyId")
            val order = getEntityById(orderId, companyId, orderRepository::getById).let(::OrderDTO)
            if(order.client != getIntPathParam(call, "clientId")) {
                throw NotFoundException("Order with id $orderId does not exist")
            }
            call.respond(HttpStatusCode.OK, order)
        }
    }
}