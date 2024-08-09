package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.edu.agh.model.ProductCreateDTO
import pl.edu.agh.repositories.ProductRepository

fun Route.productRoutes(productRepository: ProductRepository) {
    get("/product/{id}") {
        val id: Int? = call.parameters["id"]?.toInt()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        } else {
            val product = productRepository.getById(id)
            if (product == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            } else {
                call.respond(product)
                return@get
            }
        }
    }

    post("/product") {
        val product = call.receive<ProductCreateDTO>()
        try {
            productRepository.add(product)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Company with id ${product.companyId} does not exist")
            return@post
        }
        call.respond(HttpStatusCode.Created)
    }
}