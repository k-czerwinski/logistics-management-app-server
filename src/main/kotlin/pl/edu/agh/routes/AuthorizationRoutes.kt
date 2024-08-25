package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt
import pl.edu.agh.model.Company
import pl.edu.agh.model.UserRole
import pl.edu.agh.plugins.JwtProperties
import pl.edu.agh.plugins.generateToken
import pl.edu.agh.repositories.CompanyRepository
import pl.edu.agh.repositories.UserRepository

@Serializable
data class LoginRequest(val username: String, val password: String, val companyDomain: String)

@Serializable
data class LoginResponse(val accessToken: String, val companyId: Int, val userId: Int, val userRole: UserRole)

fun Route.authorizationRoutes(
    userRepository: UserRepository,
    companyRepository: CompanyRepository,
    jwtProperties: JwtProperties
) {
    post("/login") {
        val loginRequest = call.receive<LoginRequest>()
        val company : Company = companyRepository.getByDomain(loginRequest.companyDomain)
            ?: throw PermissionDeniedException("Company with domain ${loginRequest.companyDomain} does not exist")
        val user = userRepository.getByUsername(loginRequest.username, company.id)

        if (user != null && BCrypt.checkpw(loginRequest.password, user.password)) {
            val token = generateToken(jwtProperties, user.id, user.role, user.companyId)
            call.respond(HttpStatusCode.OK, LoginResponse(token, user.id, user.companyId, user.role))
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}