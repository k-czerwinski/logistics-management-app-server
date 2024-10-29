package pl.edu.agh.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.testing.ApplicationTestBuilder
import org.junit.Test
import pl.edu.agh.KtorTestBase
import pl.edu.agh.model.UserRole
import java.time.ZoneOffset
import kotlin.test.assertEquals

class ConfigureSecurityTest : KtorTestBase() {
    private val protectedRoute = "/protected-route"

    private fun verifyTokenTestApplication(test: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
        customTestApplication {
            application {
                configureSecurity(jwtProperties)
            }
            routing {
                authenticate {
                    get(protectedRoute) {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
            test(client)
        }

    @Test
    fun `test configureSecurity with invalid audience`() = verifyTokenTestApplication {
        // given
        val tokenWithInvalidAudience = JwtTokenBuilder(jwtProperties.copy(audience = "invalidAudience"))
            .accessToken(1, UserRole.CLIENT, 1)
        // when
        val response = client.get(protectedRoute) {
            bearerAuth(tokenWithInvalidAudience)
        }
        // then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `test configureSecurity with invalid domain`() = customTestApplication {
        // given
        val tokenWithInvalidDomain = JwtTokenBuilder(jwtProperties.copy(domain = "invalidDomain"))
            .accessToken(1, UserRole.CLIENT, 1)
        // when
        val response = client.get(protectedRoute) {
            bearerAuth(tokenWithInvalidDomain)
        }
        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `test configureSecurity with invalid secret`() = customTestApplication {
        // given
        val tokenWithInvalidSecret = JwtTokenBuilder(jwtProperties.copy(secret = "invalidSecret"))
            .accessToken(1, UserRole.CLIENT, 1)
        // when
        val response = client.get(protectedRoute) {
            bearerAuth(tokenWithInvalidSecret)
        }
        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `test configureSecurity with invalid realm`() = customTestApplication {
        // given
        val tokenWithInvalidRealm = JwtTokenBuilder(jwtProperties.copy(realm = "invalidRealm"))
            .accessToken(1, UserRole.CLIENT, 1)
        // when
        val response = client.get(protectedRoute) {
            bearerAuth(tokenWithInvalidRealm)
        }
        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `test configureSecurity with invalid algorithm`() = customTestApplication {
        // given
        val expiryDate = java.time.LocalDateTime.now(ZoneOffset.UTC).plusSeconds(jwtProperties.accessTokenExpiresIn.toLong() / 1000L)
        val tokenWithInvalidAlgorithm = JWT.create()
            .withAudience(jwtProperties.audience)
            .withIssuer(jwtProperties.domain)
            .withClaim("role", UserRole.CLIENT.name)
            .withClaim("user", 1)
            .withClaim("company", 1)
            .withExpiresAt(expiryDate.toInstant(ZoneOffset.UTC))
            .sign(Algorithm.HMAC384(jwtProperties.secret))
        // when
        val response = client.get(protectedRoute) {
            bearerAuth(tokenWithInvalidAlgorithm)
        }
        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `test configureSecurity with expiredToken`() = customTestApplication {
        // given
        val expiryDate = java.time.LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1)
        val expiredToken = JWT.create()
            .withAudience(jwtProperties.audience)
            .withIssuer(jwtProperties.domain)
            .withClaim("role", UserRole.CLIENT.name)
            .withClaim("user", 1)
            .withClaim("company", 1)
            .withExpiresAt(expiryDate.toInstant(ZoneOffset.UTC))
            .sign(Algorithm.HMAC384(jwtProperties.secret))
        // when
        val response = client.get(protectedRoute) {
            bearerAuth(expiredToken)
        }
        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}