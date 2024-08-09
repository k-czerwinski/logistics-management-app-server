package pl.edu.agh.repositories

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Interface for repositories.
 * T - type of the entity
 * U - type of the entity's DAO used for adding new entities
 */
interface Repository<T, U> {
    suspend fun getAll(): List<T>
    suspend fun getById(id: Int): T?
    suspend fun add(item: U): Unit
    suspend fun update(item: T): T
    suspend fun delete(id: Int): Boolean
}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)