package pl.edu.agh.plugins

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
import pl.edu.agh.KtorTestBase
import pl.edu.agh.model.UserRole
import kotlin.test.Test
import kotlin.test.assertEquals

class UserRoleAuthorizationPluginTest : KtorTestBase() {
    private val userId = 1
    private val clientProtectedRoute = "/client/$userId"
    private val courierProtectedRoute = "/courier/$userId"
    private val adminProtectedRoute = "/admin/$userId"

    private fun verifyPluginTestApplication(test: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
        customTestApplication {
            application {
                configureSecurity(jwtProperties)
            }
            routing {
                authenticate {
                    route(Regex("/(?<userRole>(client|admin|courier))/(?<userId>\\d+)")) {
                        install(UserRoleAuthorizationPlugin)
                        get {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
            }
            test(client)
        }

    @Test
    fun `userRoleAuthorizationPlugin should allow request with valid client token`() =
        performTest(HttpStatusCode.OK, UserRole.CLIENT, clientProtectedRoute)

    @Test
    fun `userRoleAuthorizationPlugin should pass request with valid role`() =
        performTest(HttpStatusCode.OK, UserRole.ADMIN, adminProtectedRoute)

    @Test
    fun `userRoleAuthorizationPlugin should pass request with valid courier token`() =
        performTest(HttpStatusCode.OK, UserRole.COURIER, courierProtectedRoute)

    @Test
    fun `userRoleAuthorizationPlugin should deny request for client path`() =
        performTest(HttpStatusCode.Unauthorized, UserRole.ADMIN, clientProtectedRoute)

    @Test
    fun `userRoleAuthorizationPlugin should deny request for courier path`() =
        performTest(HttpStatusCode.Unauthorized, UserRole.ADMIN, courierProtectedRoute)

    @Test
    fun `userRoleAuthorizationPlugin should deny request for admin path`() =
        performTest(HttpStatusCode.Unauthorized, UserRole.CLIENT, adminProtectedRoute)

    @Test
    fun `userRoleAuthorizationPlugin should deny request when no userRole in path`() = customTestApplication {
        // given
        application {
            configureSecurity(jwtProperties)
        }
        routing {
            authenticate {
                route(Regex("/(?<differentName>(client|admin|courier))/(?<userId>\\d+)")) {
                    install(UserRoleAuthorizationPlugin)
                    get {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
        // when
        val response = client.get(clientProtectedRoute.replace("{userId}", userId.toString())) {
            bearerAuth(jwtTokenBuilder.accessToken(1, UserRole.CLIENT, userId))
        }
        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    private fun performTest(expectedStatusCode: HttpStatusCode, tokenRole: UserRole, route: String) = verifyPluginTestApplication {
        // given
        val token = jwtTokenBuilder.accessToken(1, tokenRole, userId)
        // when
        val response = client.get(route.replace("{userId}", userId.toString())) {
            bearerAuth(token)
        }
        // then
        assertEquals(expectedStatusCode, response.status)
    }
}