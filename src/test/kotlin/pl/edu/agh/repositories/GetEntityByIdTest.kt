package pl.edu.agh.repositories

import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetEntityByIdTest {

    @Test
    fun `getEntityById returns entity when found`() = runTest {
        // given
        val entityId = 1
        val companyId = 1
        val expectedEntity = "Entity"
        val getEntityFunction: suspend (Int, Int) -> String? = { id, companyId -> if (id == entityId && companyId == companyId) expectedEntity else null }

        // when
        val result = getEntityById(entityId, companyId, getEntityFunction)

        // then
        assertEquals(expectedEntity, result)
    }

    @Test
    fun `getEntityById throws NotFoundException when entity not found`() = runTest {
        // given
        val entityId = 1
        val companyId = 1
        val getEntityFunction: suspend (Int, Int) -> String? = { _, _ -> null }

        // when & then
        assertFailsWith<NotFoundException> {
            getEntityById(entityId, companyId, getEntityFunction)
        }
    }
}