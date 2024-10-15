package pl.edu.agh.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pl.edu.agh.model.*
import pl.edu.agh.plugins.UserRoleAuthorizationPlugin
import pl.edu.agh.repositories.OrderRepository
import pl.edu.agh.repositories.ProductRepository
import pl.edu.agh.repositories.UserRepository

fun Route.adminRoutes(
    productRepository: ProductRepository,
    userRepository: UserRepository,
    orderRepository: OrderRepository
) {
    route(Regex("/(?<userRole>admin)/(?<adminId>\\d+)")) {
        install(UserRoleAuthorizationPlugin)
        post("/product") {
            val product = call.receive<ProductCreateDTO>()
            val companyId = getIntPathParam(call, "companyId")
            try {
                productRepository.add(product, companyId)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            call.respond(HttpStatusCode.Created)
        }

        get("/users") {
            val companyId = getIntPathParam(call, "companyId")
            val userRole: UserRole? = call.request.queryParameters["role"]?.let(UserRole::valueOfNullable)
            val userList = if (userRole != null) {
                userRepository.getByRole(companyId, userRole)
            } else {
                userRepository.getAll(companyId)
            }
            call.respond(userList.map(UserListViewItemDTO::toDTO).toList())
        }

        get("/user/{userId}") {
            val companyId: Int = getIntPathParam(call, "companyId")
            val userId: Int = getIntPathParam(call, "userId")
            val user = getEntityById(userId, companyId, userRepository::getById)
            call.respond(user)
        }

        post("/user") {
            val user = call.receive<UserCreateDTO>()
            val companyId = getIntPathParam(call,"companyId")
            userRepository.getByUsername(user.username, companyId)?.let {
                if (it.username == user.username) {
                    call.respond(HttpStatusCode.Conflict, "User with username ${user.username} already exists")
                    return@post
                }
            }
            var createdUser: User?
            try {
                createdUser = userRepository.add(user, companyId)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.UnprocessableEntity)
                return@post
            }
            call.response.header(HttpHeaders.Location, call.request.path() + "/${createdUser.id}")
            call.respond(HttpStatusCode.Created)
        }

        get("/orders") {
            val companyId: Int = getIntPathParam(call, "companyId")
            val orders = orderRepository.getAll(companyId).map(Order::toOrderListView).toList()
            call.respond(HttpStatusCode.OK, orders)
        }

        get("/order/{orderId}") {
            val orderId: Int = getIntPathParam(call, "orderId")
            val companyId: Int = getIntPathParam(call, "companyId")
            val order = getEntityById(orderId, companyId, orderRepository::getById)
            call.respond(order)
        }

        put("/order/{orderId}/send") {
            val companyId: Int = getIntPathParam(call, "companyId")
            val orderId: Int = getIntPathParam(call, "orderId")
            val courierId : Int = getIntQueryParam(call, "courierId")
            orderRepository.sendOrder(companyId, orderId, courierId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}