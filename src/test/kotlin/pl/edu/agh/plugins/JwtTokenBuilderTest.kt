package pl.edu.agh.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import pl.edu.agh.KtorTestBase
import pl.edu.agh.model.UserRole
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class JwtTokenBuilderTest : KtorTestBase() {
    @Test
    fun `test accessToken`() {
        // given
        val userId = 1
        val userRole = UserRole.CLIENT
        val companyId = 1
        val verifier = JWT.require(Algorithm.HMAC256(jwtProperties.secret))
            .withAudience(jwtProperties.audience)
            .withIssuer(jwtProperties.domain)
            .build()
        val expiryDate = java.time.LocalDateTime.now(ZoneOffset.UTC).plusSeconds(jwtProperties.accessTokenExpiresIn.toLong() / 1000L)
        // when
        val accessToken = jwtTokenBuilder.accessToken(userId, userRole, companyId)
        // then
        val decodedToken = verifier.verify(accessToken)
        decodedToken.claims.apply {
            assertEquals(userId, get("user")?.asInt())
            assertEquals(userRole.name, get("role")?.asString())
            assertEquals(companyId, get("company")?.asInt())
        }
        assertEquals(jwtProperties.audience, decodedToken.audience.first())
        assertEquals(jwtProperties.domain, decodedToken.issuer)
        assertEquals(expiryDate.toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS), decodedToken.expiresAt.toInstant())
        assertEquals("HS256", decodedToken.algorithm)
    }

    @Test
    fun `test refreshToken`() {
        // given
        val userId = 1
        val userRole = UserRole.CLIENT
        val companyId = 1
        val verifier = JWT.require(Algorithm.HMAC256(jwtProperties.secret))
            .withAudience(jwtProperties.audience)
            .withIssuer(jwtProperties.domain)
            .build()
        val expiryDate = java.time.LocalDateTime.now(ZoneOffset.UTC).plusSeconds(jwtProperties.refreshTokenExpiresIn.toLong() / 1000L)
        // when
        val refreshToken = jwtTokenBuilder.refreshToken(userId, userRole, companyId).first
        val refreshTokenExpiryDate = jwtTokenBuilder.refreshToken(userId, userRole, companyId).second
        // then
        val decodedToken = verifier.verify(refreshToken)
        decodedToken.claims.apply {
            assertEquals(userId, get("user")?.asInt())
            assertEquals(userRole.name, get("role")?.asString())
            assertEquals(companyId, get("company")?.asInt())
        }
        assertEquals(jwtProperties.audience, decodedToken.audience.first())
        assertEquals(jwtProperties.domain, decodedToken.issuer)
        assertEquals(expiryDate.toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS), decodedToken.expiresAt.toInstant())
        assertEquals("HS256", decodedToken.algorithm)

        assertEquals(expiryDate.toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS),
            refreshTokenExpiryDate.toInstant(UtcOffset.ZERO).toJavaInstant().truncatedTo(ChronoUnit.SECONDS))
    }
}