package pl.edu.agh

import io.ktor.client.HttpClient
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import pl.edu.agh.plugins.JwtProperties
import pl.edu.agh.plugins.JwtTokenBuilder

open class KtorTestBase {

    protected val applicationConfig = ApplicationConfig("api-test.conf")
    protected val jwtProperties: JwtProperties = JwtProperties(
        applicationConfig.property("ktor.security.jwt.audience").getString(),
        applicationConfig.property("ktor.security.jwt.issuer").getString(),
        applicationConfig.property("ktor.security.jwt.realm").getString(),
        applicationConfig.property("ktor.security.jwt.secret").getString(),
        applicationConfig.property("ktor.security.jwt.accessTokenValidity").getString().toInt(),
        applicationConfig.property("ktor.security.jwt.refreshTokenValidity").getString().toInt()
    )
    protected val jwtTokenBuilder: JwtTokenBuilder = JwtTokenBuilder(jwtProperties)

    protected fun customTestApplication(test: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) = testApplication {
        environment { config = applicationConfig }
        test(client)
    }
}