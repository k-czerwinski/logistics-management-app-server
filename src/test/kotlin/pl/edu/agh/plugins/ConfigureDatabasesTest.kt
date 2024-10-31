package pl.edu.agh.plugins

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import pl.edu.agh.dao.CompanyDAO
import pl.edu.agh.dao.OrderDAO
import pl.edu.agh.dao.OrderProductDAO
import pl.edu.agh.dao.ProductDAO
import pl.edu.agh.dao.RefreshTokenDAO
import pl.edu.agh.dao.UserDAO
import kotlin.test.Test
import kotlin.test.assertTrue

class ConfigureDatabasesTest{
    val dbConfig = ApplicationConfig("db-test.conf")

    @Test
    fun `test configureDatabases`() = testApplication {
        environment {
            config = dbConfig
        }
        application {
            configureDatabases()
            // then
            transaction {
                // verify tables are created
                assertTrue(TransactionManager.isInitialized())
                assertTrue(OrderDAO.all().empty())
                assertTrue(UserDAO.all().empty())
                assertTrue(CompanyDAO.all().empty())
                assertTrue(ProductDAO.all().empty())
                assertTrue(RefreshTokenDAO.all().empty())
                assertTrue(OrderProductDAO.all().empty())
            }
        }
    }
}