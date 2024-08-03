package pl.edu.agh.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    val jwtAudience = environment.config.property("ktor.security.jwt.audience").toString()
    val jwtDomain = environment.config.propertyOrNull("ktor.security.jwt.issuer").toString()
    val jwtRealm = environment.config.propertyOrNull("ktor.security.jwt.realm").toString()
    val jwtSecret = environment.config.propertyOrNull("ktor.security.jwt.secret").toString()
    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
