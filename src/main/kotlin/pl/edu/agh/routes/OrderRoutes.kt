package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.edu.agh.model.OrderCreateDTO
import pl.edu.agh.repositories.OrderRepository

fun Route.orderRoutes(orderRepository: OrderRepository) {
    get("/order/{id}") {
        val id : Int? = call.parameters["id"]?.toInt()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        } else {
            val user = orderRepository.getById(id)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            } else {
                call.respond(user)
                return@get
            }
        }
    }

    post("/order") {
        val order = call.receive<OrderCreateDTO>()
        try {
            orderRepository.add(order)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Company with id ${order.companyId} does not exist")
            return@post
        }
        call.respond(HttpStatusCode.Created)
    }
}