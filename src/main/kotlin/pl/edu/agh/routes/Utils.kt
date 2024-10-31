package pl.edu.agh.routes

import io.ktor.server.application.*
import io.ktor.server.plugins.*
class QueryParamParseException(message: String) : BadRequestException(message)
class PathParamParseException(message: String) : BadRequestException(message)
class PermissionDeniedException(message: String) : IllegalAccessException(message)

fun getIntPathParam(call: ApplicationCall, paramName: String): Int = call.parameters[paramName]?.toInt()
    ?: throw PathParamParseException("Parameter $paramName is missing or is not an integer")

fun getIntQueryParam(call: ApplicationCall, paramName: String): Int = call.request.queryParameters[paramName]?.toInt()
    ?: throw QueryParamParseException("Query parameter $paramName is missing or it is not an integer")

fun validateWithPathParam(call: ApplicationCall, requiredValue: Int, pathParamName: String) {
    val pathParamValue = getIntPathParam(call, pathParamName)
    if (pathParamValue != requiredValue) {
        throw PermissionDeniedException("Invalid value for path parameter $pathParamName")
    }
}

suspend fun <T> getEntityById(entityId: Int, companyId: Int, getEntityFunction: suspend (Int, Int) -> T?): T =
    getEntityFunction(entityId, companyId) ?: throw NotFoundException("Entity with id $entityId not found")

