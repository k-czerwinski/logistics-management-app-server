package pl.edu.agh.repositories

import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Interface for repositories.
 * T - type of the entity
 * U - type of the entity's DAO used for adding new entities
 */
interface Repository<T, U> {
    suspend fun getAll(companyId: Int): List<T>
    suspend fun getById(entityId: Int, companyId: Int): T?
    suspend fun add(item: U, companyId: Int) : T
}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

suspend fun <T> getEntityById(entityId: Int, companyId: Int, getEntityFunction: suspend (Int, Int) -> T?): T =
    getEntityFunction(entityId, companyId) ?: throw NotFoundException("Entity with id $entityId not found")