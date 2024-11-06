package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.toJavaLocalDateTime
import org.mindrot.jbcrypt.BCrypt
import pl.edu.agh.dto.LoginRequest
import pl.edu.agh.dto.LoginResponse
import pl.edu.agh.dto.LogoutRequest
import pl.edu.agh.dto.RefreshTokenRequest
import pl.edu.agh.dto.RefreshTokenResponse
import pl.edu.agh.model.Company
import pl.edu.agh.model.RefreshToken
import pl.edu.agh.plugins.JwtTokenBuilder
import pl.edu.agh.repositories.CompanyRepository
import pl.edu.agh.repositories.RefreshTokenRepository
import pl.edu.agh.repositories.UserRepository
import java.time.ZoneOffset

fun Route.authRoutes(
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
            val accessToken = jwtTokenBuilder.accessToken(user.id, user.role, user.company.id)
            val refreshToken = jwtTokenBuilder.refreshToken(user.id, user.role, user.company.id)
            refreshTokenRepository.addOrReplace(refreshToken.first, user.id, refreshToken.second)

            call.respond(
                HttpStatusCode.OK,
                LoginResponse(accessToken, refreshToken.first, user.company.id, user.id, user.role)
            )
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }

    post("/logout") {
        val refreshToken = call.receive<LogoutRequest>().refreshToken
        refreshTokenRepository.delete(refreshToken)
        call.respond(HttpStatusCode.NoContent)
    }

    post("/refresh-token") {
        val refreshToken = call.receive<RefreshTokenRequest>().refreshToken
        val refreshTokenEntry: RefreshToken = refreshTokenRepository.getByToken(refreshToken)
            ?: throw PermissionDeniedException("Invalid refresh token")

        if (java.time.LocalDateTime.now(ZoneOffset.UTC).isAfter(refreshTokenEntry.expiryDate.toJavaLocalDateTime())) {
            throw PermissionDeniedException("Refresh token expired")
        }

        val accessToken = jwtTokenBuilder.accessToken(
            refreshTokenEntry.user.id,
            refreshTokenEntry.user.role,
            refreshTokenEntry.user.company.id
        )
        val newRefreshToken = jwtTokenBuilder.refreshToken(
            refreshTokenEntry.user.id,
            refreshTokenEntry.user.role,
            refreshTokenEntry.user.company.id
        )
        refreshTokenRepository.addOrReplace(
            newRefreshToken.first,
            refreshTokenEntry.user.id,
            newRefreshToken.second
        )
        call.respond(HttpStatusCode.OK, RefreshTokenResponse(accessToken, newRefreshToken.first))
    }
}
