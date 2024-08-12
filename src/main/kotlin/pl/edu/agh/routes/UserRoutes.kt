package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.edu.agh.model.UserCreateDTO
import pl.edu.agh.repositories.UserRepository

fun Route.userRoutes(userRepository: UserRepository) {
    get("/user/{id}") {
        val id: Int? = call.parameters["id"]?.toInt()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }
        val user = userRepository.getById(id)
        if (user == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        } else {
            call.respond(user)
        }
    }

    post("/user") {
        val user = call.receive<UserCreateDTO>()
        userRepository.getByUsername(user.username, user.companyId)?.let {
            call.respond(HttpStatusCode.Conflict, "User with username ${user.username} already exists")
            return@post
        }
        try {
            userRepository.add(user)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.UnprocessableEntity)
            return@post
        }
        call.respond(HttpStatusCode.Created)
    }

}