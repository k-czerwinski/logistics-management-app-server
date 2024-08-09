package pl.edu.agh.model

import kotlinx.serialization.Serializable
import pl.edu.agh.dao.CompanyLogoDAO

@Serializable
data class CompanyLogo(
    val id: Int = -1,
    val image: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompanyLogo

        if (id != other.id) return false
        if (!image.contentEquals(other.image)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + image.contentHashCode()
        return result
    }
}

@Serializable
data class CompanyLogoCreateDTO(
    val image: ByteArray
)

fun toCompanyLogo(dao: CompanyLogoDAO) = CompanyLogo(
    id = dao.id.value,
    image = dao.image
)
