package pl.edu.agh.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.testing.ApplicationTestBuilder
import org.junit.Test
import pl.edu.agh.KtorTestBase
import pl.edu.agh.model.UserRole
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals

class PathParamAuthorizationPluginTest : KtorTestBase() {
    private val companyId = 1
    private val baseRoute = "/company/$companyId"

    private fun verifyPluginTestApplication(test: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
        customTestApplication {
            application {
                configureSecurity(jwtProperties)
            }
            routing {
                authenticate {
                    route(Regex("/company/(?<companyId>\\d+)")) {
                        install(PathParamAuthorizationPlugin("PathParamAuthorizationPlugin")) {
                            pathParameterName = "companyId"
                            jwtPrincipalClaimName = "company"
                        }
                        get {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
            }
            test(client)
        }

    @Test
    fun `pathParamAuthorizationPlugin should allow request when token complies with path param`() = verifyPluginTestApplication {
        // given
        val token = jwtTokenBuilder.accessToken(1, UserRole.ADMIN, companyId)
        // when
        val response = client.get(baseRoute.replace("{companyId}", companyId.toString())) {
            bearerAuth(token)
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `pathParamAuthorizationPlugin should deny request when token do not complies with path param`() = verifyPluginTestApplication {
        // given
        val differentCompanyId = 2
        val token = jwtTokenBuilder.accessToken(1, UserRole.ADMIN, differentCompanyId)
        // when
        val response = client.get(baseRoute.replace("{companyId}", companyId.toString())) {
            bearerAuth(token)
        }
        // then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `pathParamAuthorizationPlugin should deny request when token no specified param in path`() = customTestApplication {
        // given
        application {
            configureSecurity(jwtProperties)
        }
        routing {
            authenticate {
                route(Regex("/company/(?<differentCompanyId>\\d+)")) {
                    install(PathParamAuthorizationPlugin("PathParamAuthorizationPlugin")) {
                        pathParameterName = "companyId"
                        jwtPrincipalClaimName = "company"
                    }
                    get {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
        // when
        val response = client.get(baseRoute.replace("{companyId}", companyId.toString())) {
            bearerAuth(jwtTokenBuilder.accessToken(1, UserRole.ADMIN, companyId))
        }
        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `pathParamAuthorizationPlugin should deny request when no claim in token`() = verifyPluginTestApplication {
        // given
        val accessToken = creatAccessTokenWithClaim(null)
        // when
        val response = client.get(baseRoute.replace("{companyId}", companyId.toString())) {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `pathParamAuthorizationPlugin should deny request when wrong type in token claim`() = verifyPluginTestApplication {
        // given
        val accessToken = creatAccessTokenWithClaim("wrongType")
        // when
        val response = client.get(baseRoute.replace("{companyId}", companyId.toString())) {
            bearerAuth(accessToken)
        }
        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    private fun creatAccessTokenWithClaim(companyId: String?): String {
        val tokenBuilder = JWT.create()
            .withAudience(jwtProperties.audience)
            .withIssuer(jwtProperties.domain)
            .withClaim("role", UserRole.CLIENT.name)
            .withClaim("user", 1)
            .withExpiresAt(LocalDateTime.now().plusMinutes(1).toInstant(ZoneOffset.UTC))
        return if (companyId != null) {
            tokenBuilder.withClaim("company", companyId)
        } else {
            tokenBuilder
        }.sign(Algorithm.HMAC256(jwtProperties.secret))
    }
}