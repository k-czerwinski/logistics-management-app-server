package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt
import pl.edu.agh.model.Company
import pl.edu.agh.model.RefreshToken
import pl.edu.agh.model.UserRole
import pl.edu.agh.plugins.JwtTokenBuilder
import pl.edu.agh.plugins.PathParamAuthorizationPlugin
import pl.edu.agh.plugins.UserRoleAuthorizationPlugin
import pl.edu.agh.repositories.CompanyRepository
import pl.edu.agh.repositories.RefreshTokenRepository
import pl.edu.agh.repositories.UserRepository

@Serializable
data class LoginRequest(val username: String, val password: String, val companyDomain: String)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val companyId: Int,
    val userId: Int,
    val userRole: UserRole
)

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class RefreshTokenResponse(val accessToken: String, val refreshToken: String)

fun Route.authorizationRoutes(
    userRepository: UserRepository,
    companyRepository: CompanyRepository,
    refreshTokenRepository: RefreshTokenRepository,
    jwtTokenBuilder: JwtTokenBuilder
) {
    post("/login") {
        val loginRequest = call.receive<LoginRequest>()
        val company: Company = companyRepository.getByDomain(loginRequest.companyDomain)
            ?: throw PermissionDeniedException("Company with domain ${loginRequest.companyDomain} does not exist")
        val user = userRepository.getByUsername(loginRequest.username, company.id)

        if (user != null && BCrypt.checkpw(loginRequest.password, user.password)) {
            val accessToken = jwtTokenBuilder.accessToken(user.id, user.role, user.companyId)
            val refreshToken = jwtTokenBuilder.refreshToken(user.id, user.role, user.companyId)
            refreshTokenRepository.addOrReplace(refreshToken.first, user.id, refreshToken.second)

            call.respond(
                HttpStatusCode.OK,
                LoginResponse(accessToken, refreshToken.first, user.id, user.companyId, user.role)
            )
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }

    route(Regex("/company/(?<companyId>\\d+)/(?<userRole>(client|admin))/(?<clientId>\\d+)")) {
        post("/refresh-token") {
            val refreshToken = call.receive<RefreshTokenRequest>().refreshToken
            val refreshTokenEntry: RefreshToken = refreshTokenRepository.getByToken(refreshToken)
                ?: throw PermissionDeniedException("Invalid refresh token")
            if (refreshTokenEntry.user.id!= getIntPathParam(call, "clientId")
                || refreshTokenEntry.user.companyId != getIntPathParam(call, "companyId")) {
                throw PermissionDeniedException("Invalid refresh token")
            }

            val accessToken = jwtTokenBuilder.accessToken(refreshTokenEntry.user.id, refreshTokenEntry.user.role, refreshTokenEntry.user.companyId)
            val newRefreshToken = jwtTokenBuilder.refreshToken(refreshTokenEntry.user.id, refreshTokenEntry.user.role, refreshTokenEntry.user.companyId)
            refreshTokenRepository.addOrReplace(newRefreshToken.first, refreshTokenEntry.user.id, newRefreshToken.second)
            call.respond(HttpStatusCode.OK, RefreshTokenResponse(accessToken, newRefreshToken.first))
        }
        authenticate {
            install(PathParamAuthorizationPlugin("companyIdPathValidationPlugin")) {
                pathParameterName = "companyId"
                jwtPrincipalClaimName = "company"
            }
            install(UserRoleAuthorizationPlugin)
            install(PathParamAuthorizationPlugin("clientIdPathValidationPlugin")) {
                pathParameterName = "clientId"
                jwtPrincipalClaimName = "user"
            }

            post("/logout") {
                val clientId: Int = getIntPathParam(call, "clientId")
                refreshTokenRepository.delete(clientId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
