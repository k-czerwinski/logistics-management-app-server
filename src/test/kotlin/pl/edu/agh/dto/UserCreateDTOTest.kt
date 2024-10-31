package pl.edu.agh.dto

import org.mindrot.jbcrypt.BCrypt
import pl.edu.agh.model.UserRole
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UserCreateDTOTest {
    @Test
    fun `test create user hashes password`() {
        // given
        val password = "password"
        val userCreateDTO = UserCreateDTO("FirstName", "LastName", "username", password, UserRole.CLIENT)

        // when
        val hashedPassword = userCreateDTO.hashedPassword()

        // then
        assertNotEquals(password, hashedPassword)
        assertTrue(BCrypt.checkpw(password, hashedPassword))
    }
}