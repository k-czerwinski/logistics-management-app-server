package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
import pl.edu.agh.model.UserCreateDTO
import pl.edu.agh.repositories.UserRepository

fun Route.userRoutes(userRepository: UserRepository) {
    get("/user/{id}") {
        val id : Int? = call.parameters["id"]?.toInt()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        } else {
            val user = userRepository.getById(id)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            } else {
                call.respond(user)
                return@get
            }
        }
    }

    post("/user") {
        val user = call.receive<UserCreateDTO>()
        try {
            userRepository.add(user)
        } catch (e: EntityNotFoundException) {
            call.respond(HttpStatusCode.BadRequest, "Company with id ${user.companyId} does not exist")
            return@post
        }

        call.respond(HttpStatusCode.Created)
    }

}