package pl.edu.agh

import io.ktor.server.application.*
import io.ktor.server.netty.*
import pl.edu.agh.plugins.configureDatabases
import pl.edu.agh.plugins.configureRouting
import pl.edu.agh.plugins.configureSecurity
import pl.edu.agh.plugins.configureSerialization

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureSecurity()
    configureSerialization()
    configureDatabases()
    configureRouting()
}