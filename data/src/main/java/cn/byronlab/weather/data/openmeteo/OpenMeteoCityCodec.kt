package cn.byronlab.weather.data.openmeteo

import cn.byronlab.weather.domain.model.City
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale

internal object OpenMeteoCityCodec {

    private const val PREFIX = "openmeteo"
    private const val ENCODING = "UTF-8"

    fun encode(city: OpenMeteoCity): String {
        return listOf(
            PREFIX,
            city.geonameId,
            coordinate(city.latitude),
            coordinate(city.longitude),
            city.timezone,
            city.name,
            city.nameEn,
            city.country,
            city.admin1,
        ).joinToString(separator = "|", transform = ::encodePart)
    }

    fun decode(cityId: String?): OpenMeteoCity? {
        if (cityId.isNullOrBlank()) {
            return null
        }
        return try {
            decodeValue(cityId)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun decodeValue(cityId: String): OpenMeteoCity? {
        val parts = cityId.split("|")
        if (parts.size < 9 || decodePart(parts[0]) != PREFIX) {
            return null
        }
        val latitude = decodePart(parts[2]).toDoubleOrNull() ?: return null
        val longitude = decodePart(parts[3]).toDoubleOrNull() ?: return null
        return OpenMeteoCity(
            geonameId = decodePart(parts[1]),
            latitude = latitude,
            longitude = longitude,
            timezone = decodePart(parts[4]).ifBlank { "auto" },
            name = decodePart(parts[5]),
            nameEn = decodePart(parts[6]),
            country = decodePart(parts[7]),
            admin1 = decodePart(parts[8]),
        )
    }

    fun toDomainCity(city: OpenMeteoCity): City {
        val parent = listOf(city.admin1, city.country)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" · ")
        return City(
            cityId = encode(city),
            name = city.name,
            nameEn = city.nameEn,
            root = city.country,
            parent = parent,
            longitude = coordinate(city.longitude),
            latitude = coordinate(city.latitude),
        )
    }

    private fun encodePart(value: String): String {
        return URLEncoder.encode(value, ENCODING)
    }

    private fun decodePart(value: String): String {
        return URLDecoder.decode(value, ENCODING)
    }

    private fun coordinate(value: Double): String {
        return String.format(Locale.US, "%.4f", value)
    }
}
