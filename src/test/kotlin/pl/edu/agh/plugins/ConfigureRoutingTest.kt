package pl.edu.agh.plugins

import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Route
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pl.edu.agh.KtorTestBase
import pl.edu.agh.model.Company
import pl.edu.agh.model.UserRole
import pl.edu.agh.repositories.CompanyRepository
import pl.edu.agh.repositories.OrderRepository
import pl.edu.agh.repositories.ProductRepository
import pl.edu.agh.repositories.RefreshTokenRepository
import pl.edu.agh.repositories.UserRepository
import pl.edu.agh.routes.PermissionDeniedException
import pl.edu.agh.routes.adminRoutes
import pl.edu.agh.routes.authRoutes
import pl.edu.agh.routes.clientRoutes
import pl.edu.agh.routes.commonRoutes
import pl.edu.agh.routes.courierRoutes
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigureRoutingTest : KtorTestBase() {
    private val userRepository = mockk<UserRepository>()
    private val companyRepository = mockk<CompanyRepository>()
    private val productRepository = mockk<ProductRepository>()
    private val orderRepository = mockk<OrderRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()

    @Test
    fun `configureRouting should create application routes`() = customTestApplication {
        // given
        mockkStatic("pl.edu.agh.routes.AuthorizationRoutesKt")
        mockkStatic("pl.edu.agh.routes.CommonRoutesKt")
        mockkStatic("pl.edu.agh.routes.ClientRoutesKt")
        mockkStatic("pl.edu.agh.routes.CourierRoutesKt")
        mockkStatic("pl.edu.agh.routes.AdminRoutesKt")

        application {

            // when
            configureSecurity(jwtProperties)
            configureRouting(
                jwtTokenBuilder,
                userRepository,
                companyRepository,
                productRepository,
                orderRepository,
                refreshTokenRepository
            )

            // then
            verify {
                any<Route>().authRoutes(
                    userRepository,
                    companyRepository,
                    refreshTokenRepository,
                    jwtTokenBuilder
                )
            }
            verify { any<Route>().commonRoutes(productRepository, userRepository) }
            verify { any<Route>().clientRoutes(orderRepository) }
            verify { any<Route>().courierRoutes(orderRepository) }
            verify { any<Route>().adminRoutes(productRepository, userRepository, orderRepository) }
        }
    }

    @Test
    fun `routes created by configureRouting should require authentication`() = customTestApplication {
        // given
        val company = Company(1, "Company 1", "sample-company.com")
        coEvery { companyRepository.getById(1, 1) } returns company
        application {
            configureSecurity(jwtProperties)
            configureRouting(
                jwtTokenBuilder,
                userRepository,
                companyRepository,
                productRepository,
                orderRepository,
                refreshTokenRepository
            )
        }
        // when
        val response = client.get("/company/1") {
            bearerAuth(jwtTokenBuilder.accessToken(1, UserRole.CLIENT, 1))
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(Json.encodeToString(company), response.bodyAsText())
    }

    @Test
    fun `configureRouting should configure company path param validation plugin`() = customTestApplication {
        // given
        // different companyId in path and token
        val token = jwtTokenBuilder.accessToken(1, UserRole.CLIENT, 1)
        val getCompanyPath = "/company/2"
        application {
            configureSecurity(jwtProperties)
            configureRouting(
                jwtTokenBuilder,
                userRepository,
                companyRepository,
                productRepository,
                orderRepository,
                refreshTokenRepository
            )
        }
        // when
        val response = client.get(getCompanyPath) {
            bearerAuth(token)
        }
        // then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `configureRouting should configure routes to require token`() = customTestApplication {
        // given
        application {
            configureSecurity(jwtProperties)
            configureRouting(
                jwtTokenBuilder,
                userRepository,
                companyRepository,
                productRepository,
                orderRepository,
                refreshTokenRepository
            )
        }
        // when
        // no token in request
        val response = client.get("/company/1")
        // then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `configureRouting should configure routes to check token`() = customTestApplication {
        // given
        var invalidToken = "invalidToken"
        application {
            configureSecurity(jwtProperties)
            configureRouting(
                jwtTokenBuilder,
                userRepository,
                companyRepository,
                productRepository,
                orderRepository,
                refreshTokenRepository
            )
        }
        // when
        val response = client.get("/company/1") {
            bearerAuth(invalidToken)
        }
        // then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `configureRouting should configure mapping for JwtWithoutRequiredClaimException`() = customTestApplication {
        // given
        coEvery { companyRepository.getById(1, 1) } throws JwtWithoutRequiredClaimException("claimName")
        application {
            configureSecurity(jwtProperties)
            configureRouting(
                jwtTokenBuilder,
                userRepository,
                companyRepository,
                productRepository,
                orderRepository,
                refreshTokenRepository
            )
        }
        // when
        val response = client.get("/company/1") {
            bearerAuth(jwtTokenBuilder.accessToken(1, UserRole.CLIENT, 1))
        }
        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `configureRouting should configure mapping for PermissionDeniedException`() = customTestApplication {
        // given
        coEvery { companyRepository.getById(1, 1) } throws PermissionDeniedException("message")
        application {
            configureSecurity(jwtProperties)
            configureRouting(
                jwtTokenBuilder,
                userRepository,
                companyRepository,
                productRepository,
                orderRepository,
                refreshTokenRepository
            )
        }
        // when
        val response = client.get("/company/1") {
            bearerAuth(jwtTokenBuilder.accessToken(1, UserRole.CLIENT, 1))
        }
        // then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}