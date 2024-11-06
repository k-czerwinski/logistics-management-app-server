package pl.edu.agh.routes

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.ApplicationTestBuilder
import io.mockk.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mindrot.jbcrypt.BCrypt
import pl.edu.agh.KtorTestBase
import pl.edu.agh.dao.RefreshTokenDAO
import pl.edu.agh.dto.LoginRequest
import pl.edu.agh.dto.LoginResponse
import pl.edu.agh.dto.LogoutRequest
import pl.edu.agh.dto.RefreshTokenRequest
import pl.edu.agh.dto.RefreshTokenResponse
import pl.edu.agh.model.*
import pl.edu.agh.plugins.JwtTokenBuilder
import pl.edu.agh.plugins.configureSecurity
import pl.edu.agh.repositories.CompanyRepository
import pl.edu.agh.repositories.RefreshTokenRepository
import pl.edu.agh.repositories.UserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthorizationRoutesTest : KtorTestBase() {
    private val userRepository = mockk<UserRepository>()
    private val companyRepository = mockk<CompanyRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val jwtTokenBuilderMock = mockk<JwtTokenBuilder>()

    private val companyId = 1
    private val userId = 1
    private val companyDomain = "sample-company.com"
    private val username = "username"
    private val password = "password"
    private val userRole = UserRole.CLIENT

    val company = Company(companyId, "Company 1", companyDomain)
    val user = User(userId, "FirstName", "LastName", username, BCrypt.hashpw(password, BCrypt.gensalt()), userRole, company)

    private fun authorizationRoutesTestApplication(test: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
        customTestApplication {
            application {
                configureSecurity(jwtProperties)
                install(ContentNegotiation) {
                    json()
                }
            }
            routing {
                authRoutes(userRepository, companyRepository, refreshTokenRepository, jwtTokenBuilderMock)
            }
            test(client)
        }

    @Test
    fun `test login with valid credentials`() = authorizationRoutesTestApplication {
        // given
        val loginRequest = LoginRequest(username, password, companyDomain)
        val accessToken = jwtTokenBuilder.accessToken(userId, userRole, companyId)
        val refreshToken = jwtTokenBuilder.refreshToken(userId, userRole, companyId)

        coEvery { jwtTokenBuilderMock.accessToken(userId, userRole, companyId) } returns accessToken
        coEvery { jwtTokenBuilderMock.refreshToken(userId, userRole, companyId) } returns refreshToken
        coEvery { companyRepository.getByDomain(companyDomain) } returns company
        coEvery { userRepository.getByUsername(username, companyId) } returns user
        coEvery { refreshTokenRepository.addOrReplace(any(), any(), any()) } returns mockk<RefreshTokenDAO>()
        // when
        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(loginRequest))
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        val loginResponse = Json.decodeFromString<LoginResponse>(response.bodyAsText())
//        assertEquals(accessToken, loginResponse.accessToken)
        assertEquals(refreshToken.first, loginResponse.refreshToken)
        assertEquals(companyId, loginResponse.companyId)
        assertEquals(userId, loginResponse.userId)
        assertEquals(userRole, loginResponse.userRole)
    }

    @Test
    fun `test login with invalid credentials`() = authorizationRoutesTestApplication {
        // given
        val loginRequest = LoginRequest(username, "wrongPassword", companyDomain)
        coEvery { companyRepository.getByDomain(companyDomain) } returns company
        coEvery { userRepository.getByUsername(username, companyId) } returns user
        // when
        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(loginRequest))
        }
        // then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `test logout`() = authorizationRoutesTestApplication {
        // given
        val refreshToken = "someRefreshToken"
        coEvery { refreshTokenRepository.delete(refreshToken) } returns Unit
        // when
        val response = client.post("/logout") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(LogoutRequest(refreshToken)))
        }
        // then
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `test refresh token with valid token`() = authorizationRoutesTestApplication {
        // given
        val refreshTokenPair = jwtTokenBuilder.refreshToken(userId, userRole, companyId)
        val refreshTokenEntry = RefreshToken(user, refreshTokenPair.first, refreshTokenPair.second)
        val newAccessToken = jwtTokenBuilder.accessToken(userId, userRole, companyId)
        val newRefreshToken = jwtTokenBuilder.refreshToken(userId, userRole, companyId).first
        coEvery { jwtTokenBuilderMock.accessToken(userId, userRole, companyId) } returns newAccessToken
        coEvery { jwtTokenBuilderMock.refreshToken(userId, userRole, companyId) } returns refreshTokenPair
        coEvery { refreshTokenRepository.getByToken(refreshTokenEntry.token) } returns refreshTokenEntry
        coEvery { refreshTokenRepository.addOrReplace(any(), any(), any()) } returns mockk<RefreshTokenDAO>()
        // when
        val response = client.post("/refresh-token") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(RefreshTokenRequest(refreshTokenEntry.token)))
        }
        // then
        assertEquals(HttpStatusCode.OK, response.status)
        val refreshTokenResponse = Json.decodeFromString<RefreshTokenResponse>(response.bodyAsText())
        assertEquals(newAccessToken, refreshTokenResponse.accessToken)
        assertEquals(newRefreshToken, refreshTokenResponse.refreshToken)
    }

    @Test
    fun `test refresh token with expired token`() = authorizationRoutesTestApplication {
        // given
        val refreshToken = jwtTokenBuilder.refreshToken(userId, userRole, companyId).first
        val refreshTokenEntry = RefreshToken(user, refreshToken, LocalDateTime(2020, 10, 1, 10, 0))
        coEvery { refreshTokenRepository.getByToken(refreshToken) } returns refreshTokenEntry
        // then
        assertFailsWith<PermissionDeniedException>("Refresh token expired") {
            client.post("/refresh-token") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(RefreshTokenRequest(refreshToken)))
            }
        }
    }
}