package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt
import pl.edu.agh.plugins.JwtProperties
import pl.edu.agh.plugins.generateToken
import pl.edu.agh.repositories.UserRepository

@Serializable
data class LoginDTO(val username: String, val password: String, val companyId: Int)
@Serializable
data class TokenDTO(val accessToken: String)

fun Route.authorizationRoutes(userRepository: UserRepository, jwtProperties: JwtProperties) {
    post("/login") {
        val loginRequest = call.receive<LoginDTO>()
        val user = userRepository.getByUsername(loginRequest.username, loginRequest.companyId)

        if (user != null && BCrypt.checkpw(loginRequest.password, user.password)) {
            val token = generateToken(jwtProperties, user.username, user.role, user.company)
            call.respond(HttpStatusCode.OK, TokenDTO(token))
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}