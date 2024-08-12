package pl.edu.agh.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import pl.edu.agh.model.Company
import pl.edu.agh.model.UserRole

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
    createConfiguration = ::PluginConfiguration
) {
    pluginConfig.apply {
        on(AuthenticationChecked) { call ->
            val companyIdPathParameter: Int? = call.parameters["companyId"]?.toInt()
            val principal = call.principal<JWTPrincipal>()
            val companyIdFromToken: Int? = principal?.payload?.getClaim("company")?.asInt()
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

class PluginConfiguration {
    lateinit var pathParameterName: String
    lateinit var jwtPrincipalClaimName: String
}

fun generateToken(jwtProperties: JwtProperties, username: String, role: UserRole, company: Company): String {
    return JWT.create()
        .withAudience(jwtProperties.audience)
        .withIssuer(jwtProperties.domain)
        .withClaim("role", role.name)
        .withClaim("username", username)
        .withClaim("company", company.id)
        .sign(Algorithm.HMAC256(jwtProperties.secret))
}
