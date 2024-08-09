package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.edu.agh.model.CompanyCreateDTO
import pl.edu.agh.repositories.CompanyRepository

fun Route.companyRoutes(companyRepository: CompanyRepository) {
    get("/company/{id}") {
        val id: Int? = call.parameters["id"]?.toInt()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        } else {
            val company = companyRepository.getById(id)
            if (company == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            } else {
                call.respond(company)
                return@get
            }
        }
    }

    post("/company") {
        val company = call.receive<CompanyCreateDTO>()
        companyRepository.add(company)
        call.respond(HttpStatusCode.Created)
    }
}