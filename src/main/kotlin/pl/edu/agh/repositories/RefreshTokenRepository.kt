package pl.edu.agh.repositories

import kotlinx.datetime.LocalDateTime
import pl.edu.agh.dao.RefreshTokenDAO
import pl.edu.agh.dao.RefreshTokenTable
import pl.edu.agh.dao.UserDAO
import pl.edu.agh.model.RefreshToken
import pl.edu.agh.model.User

class RefreshTokenRepository {
    suspend fun addOrReplace(refreshToken: String, userId: Int, expiryDate: LocalDateTime) : RefreshTokenDAO = suspendTransaction {
        RefreshTokenDAO.findById(userId)?.delete()
        RefreshTokenDAO.new {
            token = refreshToken
            user = UserDAO[userId]
            this.expiryDate = expiryDate
        }
    }

    suspend fun getByToken(token: String): RefreshToken? = suspendTransaction {
        RefreshTokenDAO.find { RefreshTokenTable.token eq token }.firstOrNull()?.let {
            RefreshToken(User(it.user), it.token, it.expiryDate)
        }
    }

    suspend fun delete(refreshToken: String) = suspendTransaction{
        RefreshTokenDAO.find { RefreshTokenTable.token eq refreshToken }.firstOrNull()?.delete()
    }
}