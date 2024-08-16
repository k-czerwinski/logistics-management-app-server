package pl.edu.agh.routes

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import pl.edu.agh.model.User
import pl.edu.agh.plugins.getClaimFromToken
import pl.edu.agh.repositories.UserRepository

class PathParamParseException(message: String) : BadRequestException(message)
class PermissionDeniedException(message: String) : IllegalAccessException(message)

fun getIntPathParam(call: ApplicationCall, paramName: String): Int = call.parameters[paramName]?.toInt()
    ?: throw PathParamParseException("Parameter $paramName is missing or is not an integer")

fun validateWithPathParam(call: ApplicationCall, requiredValue: Int, pathParamName: String) {
    val pathParamValue = getIntPathParam(call, pathParamName)
    if (pathParamValue != requiredValue) {
        throw PermissionDeniedException("Invalid value for path parameter $pathParamName")
    }
}

suspend fun <T> getEntityById(entityId: Int, companyId: Int, getEntityFunction: suspend (Int, Int) -> T?): T =
    getEntityFunction(entityId, companyId) ?: throw NotFoundException("Entity with id $entityId not found")

suspend fun currentUserEndpoint(call: ApplicationCall, userRepository: UserRepository): User {
    val userId = getClaimFromToken(call, "user").asInt()
    val companyId: Int = getIntPathParam(call, "companyId")
    return getEntityById(userId, companyId, userRepository::getById)

}

