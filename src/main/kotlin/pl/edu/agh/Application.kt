package pl.edu.agh

import io.ktor.server.application.*
import io.ktor.server.netty.*
import pl.edu.agh.plugins.*
import pl.edu.agh.repositories.*

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val jwtAudience = environment.config.property("ktor.security.jwt.audience").getString()
    val jwtDomain = environment.config.property("ktor.security.jwt.issuer").getString()
    val jwtRealm = environment.config.property("ktor.security.jwt.realm").getString()
    val jwtSecret = environment.config.property("ktor.security.jwt.secret").getString()
    val accessTokenExpiresIn = environment.config.property("ktor.security.jwt.accessTokenValidity").getString().toInt()
    val refreshTokenExpiresIn = environment.config.property("ktor.security.jwt.refreshTokenValidity").getString().toInt()
    val jwtProperties = JwtProperties(jwtAudience, jwtDomain, jwtRealm, jwtSecret, accessTokenExpiresIn, refreshTokenExpiresIn)

    val productRepository = ProductRepository()
    val orderRepository = OrderRepository()
    val userRepository = UserRepository()
    val companyRepository = CompanyRepository()
    val refreshTokenRepository = RefreshTokenRepository()
    val jwtTokenBuilder = JwtTokenBuilder(jwtProperties)
    configureDatabases()
    configureSecurity(jwtProperties)
    configureRouting(jwtTokenBuilder, userRepository, companyRepository, productRepository, orderRepository, refreshTokenRepository)
}