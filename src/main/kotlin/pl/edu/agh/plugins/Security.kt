package pl.edu.agh.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Claim
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.*
import pl.edu.agh.model.UserRole
import pl.edu.agh.routes.PathParamParseException

data class JwtProperties(val audience: String, val domain: String, val realm: String, val secret: String)

fun Application.configureSecurity(jwtProperties: JwtProperties) {
    authentication {
        jwt {
            realm = jwtProperties.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtProperties.secret))
                    .withAudience(jwtProperties.audience)
                    .withIssuer(jwtProperties.domain)
                    .build()
            )
            validate { credential ->
                credential.payload.audience
                if (credential.payload.audience.contains(jwtProperties.audience)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}

val PathParamAuthorizationPlugin = createRouteScopedPlugin(
    name = "CompanyIdPathParamAuthorizationPlugin",
    createConfiguration = ::PathParameterAuthorizationPluginConfiguration
) {
    pluginConfig.apply {
        on(AuthenticationChecked) { call ->
            val companyIdPathParameter: Int? = call.parameters[pathParameterName]?.toInt()
            val companyIdFromToken: Int? = getClaimFromToken(call, jwtPrincipalClaimName).asInt()
            if (companyIdPathParameter == null || companyIdFromToken == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@on
            }
            if (companyIdPathParameter != companyIdFromToken) {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}

class PathParameterAuthorizationPluginConfiguration {
    lateinit var pathParameterName: String
    lateinit var jwtPrincipalClaimName: String
}

val UserRoleAuthorizationPlugin = createRouteScopedPlugin(
    name = "UserRoleAuthorizationPlugin"
) {
    pluginConfig.apply {
        on(AuthenticationChecked) { call ->
            val roleFromToken: UserRole? = getClaimFromToken(call, "role").asString()?.let { UserRole.valueOf(it) }
            val roleInPath: UserRole =
                call.parameters["userRole"]?.let { UserRole.valueOf(it.toUpperCasePreservingASCIIRules()) }
                    ?: throw PathParamParseException("Parameter userRole is missing or is not a valid role")

            if (roleFromToken == null || roleInPath != roleFromToken) {
                call.respond(HttpStatusCode.Unauthorized)
                return@on
            }
        }
    }
}

class JwtWithoutRequiredClaimException(claimName: String) : IllegalAccessException("No claim $claimName in token")

fun getClaimFromToken(call: ApplicationCall, claimName: String): Claim {
    val principal = call.principal<JWTPrincipal>()
    return principal?.payload?.getClaim(claimName)
        ?: throw JwtWithoutRequiredClaimException("No claim $claimName in token")
}

fun generateToken(jwtProperties: JwtProperties, userId: Int, role: UserRole, companyId: Int): String {
    return JWT.create()
        .withAudience(jwtProperties.audience)
        .withIssuer(jwtProperties.domain)
        .withClaim("role", role.name)
        .withClaim("user", userId)
        .withClaim("company", companyId)
        .sign(Algorithm.HMAC256(jwtProperties.secret))
}
